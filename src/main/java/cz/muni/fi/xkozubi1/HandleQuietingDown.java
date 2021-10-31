package cz.muni.fi.xkozubi1;

import hudson.model.Computer;
import hudson.model.Queue;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.List;
import java.util.OptionalLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class HandleQuietingDown {
    private final Computer computer;
    private final Jenkins jenkinsInstance;
    private static Logger logger = Logger.getLogger(HandleQuietingDown.class.getName());

    HandleQuietingDown(Computer computer) {
        this.computer = computer;
        this.jenkinsInstance = Jenkins.getInstanceOrNull();
    }

    public void handleLogic() throws InterruptedException {
        long idleExecutorsCount = getIdleExecutorsCount();
        if (idleExecutorsCount == 0) {
            logger.warning("No idle executors are available");
            return;
        }

        long longestRemainingTime = getLongestExecutorRemainingTime();
        if (longestRemainingTime <= -1L) { // check whether all executors are idle, i.e. longest task finished
            logger.info("Longest task has finished.");
            return;
        }

        double ratio = ShutdownQueueConfiguration.getInstance().getPermeability();
        String strategyOption = ShutdownQueueConfiguration.getInstance().getStrategyOption();

        if (strategyOption.equals("classic")) {
            logger.info("Doing classic strategy.");
            strategyClassic(idleExecutorsCount, longestRemainingTime, ratio);
        }
        else if (strategyOption.equals("removeLonger")) {
            logger.info("Doing removeLonger strategy");
            strategyRemoveLonger(longestRemainingTime, ratio);
        }
    }

    private void strategyClassic(long idleExecutorsCount, long longestRemainingTime, double ratio) throws InterruptedException {
        List<Long> whiteListIDs = getWhiteListIDs(longestRemainingTime, ratio, idleExecutorsCount);

        if (whiteListIDs.size() == 0) {
            logger.info("No tasks satisfy condition.");
            return;
        }

        Queue.BuildableItem[] buildablesCopy = getCopyBuildables();
        logBuildablesCopy(buildablesCopy);

        logger.info("Executor longest remaining time: " + longestRemainingTime +
                "\nIdle executors count: " + idleExecutorsCount +
                "\nwhiteListIDs count: " + whiteListIDs.size());

        cancelTasksBut(whiteListIDs);
        cancelAndDoQuietDown(ShutdownQueueConfiguration.getInstance().getTimeOpenQueueMillis());
        putTasksBackToQueueBut(buildablesCopy, whiteListIDs);
    }

    private void strategyRemoveLonger(long longestRemainingTime, double ratio) throws InterruptedException {
        if(longestRemainingTime != 0) {
            cancelTasksLongerThan(longestRemainingTime, ratio);
        }

        cancelAndDoQuietDown(ShutdownQueueConfiguration.getInstance().getTimeOpenQueueMillis());
    }

    private long getLongestExecutorRemainingTime() {
        OptionalLong maxRemainingTime = computer.getExecutors()
                .stream()
                .mapToLong(e -> e.getEstimatedRemainingTimeMillis())
                .max();

        return maxRemainingTime.isPresent() ? maxRemainingTime.getAsLong() : -1L;
    }

    private long getIdleExecutorsCount() {
        return computer.getExecutors()
                .stream()
                .filter(e -> e.isIdle())
                .count();
    }

    private void cancelTasksLongerThan(long longestExecutorTime, double ratio) {
        Queue queue = Jenkins.get().getQueue();

        queue.getBuildableItems()
                .stream()
                .filter(b -> b.task.getEstimatedDuration() * ratio >= longestExecutorTime)
                .forEach(buildableItem -> {
                    printCancelTaskInfo(buildableItem);
                    queue.cancel(buildableItem);
                });
    }

    private void cancelTasksBut(List<Long> whitelist) {
        Queue queue = Jenkins.get().getQueue();

        queue.getBuildableItems()
                .stream()
                .filter(b -> !whitelist.contains(b.getId()))
                .forEach(b -> queue.cancel(b));
    }

    private List<Long> getWhiteListIDs(long longestExecutorTime, double ratio, long idleExecutorsCount) {
        return Jenkins.get().getQueue().getBuildableItems()
                .stream()
                .filter(b -> b.task.getEstimatedDuration() != -1L
                        && b.task.getEstimatedDuration() * ratio < longestExecutorTime)
                .map(b -> b.getId())
                .limit(idleExecutorsCount)
                .collect(Collectors.toList());
    }

    private void printCancelTaskInfo(Queue.BuildableItem item) {
        logger.info("Canceling task " + item.task.getName() + " with an estimated duration " + item.task.getEstimatedDuration());
    }

    private Queue.BuildableItem[] getCopyBuildables() {
        Collection<Queue.BuildableItem> buildables = Jenkins.get().getQueue().getBuildableItems();
        return buildables.toArray(new Queue.BuildableItem[buildables.size()]);
    }

    private void logBuildablesCopy(Queue.BuildableItem[] items) {
        logger.info("BUILDABLE ITEMS COPY\n");
        for (Queue.BuildableItem item : items) {
            logger.info("Task name: " + item.task.getName() + " ID: " + item.getId());
        }

        //could use Arrays.stream()...
    }

    private void cancelAndDoQuietDown(long timeMillis) throws InterruptedException {
        if (jenkinsInstance.getQueue().getBuildableItems().size() > 0)
        {
            Utils.canAddToQueue = false;
            logger.warning("Canceling shutdown for " + timeMillis + " milliseconds.");
            jenkinsInstance.doCancelQuietDown();
            Thread.sleep(timeMillis);
            logger.warning("Start shutdown.");
            jenkinsInstance.doQuietDown();
        }

        Utils.canAddToQueue = false;
    }

    private void putTasksBackToQueueBut(Queue.BuildableItem[] buildablesCopy, List<Long> whiteListIDs) {
        for (Queue.BuildableItem item : buildablesCopy) {
            if (!whiteListIDs.contains(item.getId())) {
                logger.info("Adding " + item.task.getName() + " back to the queue.");
                jenkinsInstance.getQueue().schedule(item.task, 0);
            }
        }
    }
}
