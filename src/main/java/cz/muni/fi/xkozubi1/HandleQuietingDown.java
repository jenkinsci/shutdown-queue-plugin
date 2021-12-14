package cz.muni.fi.xkozubi1;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Computer;
import hudson.model.Queue;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.List;
import java.util.OptionalLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Based on a strategy type, appropriate logic is performed.
 * @Author Dominik Kozubik
 */
public class HandleQuietingDown {
    private final Computer computer;
    private final Jenkins jenkinsInstance;
    private static Logger logger = Logger.getLogger(HandleQuietingDown.class.getName());

    HandleQuietingDown(Computer computer) {
        this.computer = computer;
        this.jenkinsInstance = Jenkins.getInstanceOrNull();
    }

    /**
     * The main function, calls other methods based on obtained strategy type if there is any idle executor.
     * @throws InterruptedException
     */
    public void handleLogic() throws InterruptedException {
        long idleExecutorsCount = getIdleExecutorsCount();
        if (idleExecutorsCount == 0) {
            logger.warning("No idle executor is available");
            return;
        }

        long longestRemainingTime = getLongestExecutorRemainingTime();
        if (longestRemainingTime <= -1L) { // check whether all executors are idle, i.e. longest task finished
            logger.info("The longest task has finished.");
            return;
        }

        double ratio = ShutdownQueueConfiguration.getInstance().getPermeability();
        String strategyOption = ShutdownQueueConfiguration.getInstance().getStrategyOption();

        if (strategyOption.equals("default")) {
            logger.info("Performing <Default> strategy.");
            strategyDefault(idleExecutorsCount, longestRemainingTime, ratio);
        }
        else if (strategyOption.equals("removeLonger")) {
            logger.info("Performing <Remove longer> strategy");
            strategyRemoveLonger(longestRemainingTime, ratio);
        }
        else if (strategyOption.equals("sortRemoveLonger")) {
            logger.info("Performing <Sort and remove longer> strategy");

            Utils.handleSorterOn(true);
            jenkinsInstance.getQueue().getSorter().sortBuildableItems(jenkinsInstance.getQueue().getBuildableItems());

            // first it sorts buildables and then does the same as remove longer strategy
            strategyRemoveLonger(longestRemainingTime, ratio);
            Utils.doReset();
        }
    }

    /**
     * Performs Default strategy.
     * @param idleExecutorsCount the number of idle executors
     * @param longestExecutorTime executor's longest estimated duration
     * @param ratio permeability value from the settings
     * @throws InterruptedException
     */
    private void strategyDefault(long idleExecutorsCount, long longestExecutorTime, double ratio) throws InterruptedException {
        List<Long> whiteListIDs = getWhiteListIDs(longestExecutorTime, ratio, idleExecutorsCount);

        if (whiteListIDs.size() == 0) {
            logger.info("No tasks satisfy condition.");
            return;
        }

        Queue.BuildableItem[] buildablesCopy = getBuildablesCopy();
        logBuildablesCopy(buildablesCopy);

        logger.info("Executor longest remaining time: " + longestExecutorTime +
                "\nIdle executors count: " + idleExecutorsCount +
                "\nwhiteListIDs count: " + whiteListIDs.size());

        cancelTasksButWhitelist(whiteListIDs);
        cancelAndDoQuietDown(ShutdownQueueConfiguration.getInstance().getTimeOpenQueueMillis());
        putTasksBackToQueueBut(buildablesCopy, whiteListIDs);
    }

    /**
     * This method performs "remove longer strategy"
     * @param longestExecutorTime executor's longest estimated duration
     * @param ratio permeability value from the settings
     * @throws InterruptedException
     */
    private void strategyRemoveLonger(long longestExecutorTime, double ratio) throws InterruptedException {
        if (longestExecutorTime != 0) {
            cancelTasksLongerThan(longestExecutorTime, ratio);
        }

        cancelAndDoQuietDown(ShutdownQueueConfiguration.getInstance().getTimeOpenQueueMillis());
    }

    /**
     * @return the longest estimated remaining time of all executors
     */
    private long getLongestExecutorRemainingTime() {
        OptionalLong maxRemainingTime = computer.getExecutors()
                .stream()
                .mapToLong(e -> e.getEstimatedRemainingTimeMillis())
                .max();

        return maxRemainingTime.isPresent() ? maxRemainingTime.getAsLong() : -1L;
    }

    /**
     * @return the number of idle executors
     */
    private long getIdleExecutorsCount() {
        return computer.getExecutors()
                .stream()
                .filter(e -> e.isIdle())
                .count();
    }

    /**
     * Cancels tasks from buildable queue which estimated duration * ratio is longer than longestExecutorTime
     * @param longestExecutorTime executor's longest estimated duration
     * @param ratio "permeability" value from the settings
     */
    private void cancelTasksLongerThan(long longestExecutorTime, double ratio) {
        Queue queue = Jenkins.get().getQueue();

        queue.getBuildableItems()
                .stream()
                .filter(b -> b.task.getEstimatedDuration() * ratio >= longestExecutorTime)
                .forEach(buildableItem -> {
                    logCancelTaskInfo(buildableItem);
                    queue.cancel(buildableItem);
                });
    }

    /**
     * Cancels tasks from the buildable queue which are not in the whitelist
     * @param whitelistIDs list of BuildableItems' IDs which are allowed to stay in the BuildableQueue
     */
    private void cancelTasksButWhitelist(List<Long> whitelistIDs) {
        Queue queue = Jenkins.get().getQueue();

        queue.getBuildableItems()
                .stream()
                .filter(b -> !whitelistIDs.contains(b.getId()))
                .forEach(b -> queue.cancel(b));
    }

    /**
     * @param longestExecutorTime executor's longest estimated duration
     * @param ratio permeability value from the settings
     * @param idleExecutorsCount the number of idle executors
     * @returns list of BuildableItems' IDs which satisfy conditions. Length of the list is less than or equal to the
     * number of idle executors.
     */
    private List<Long> getWhiteListIDs(long longestExecutorTime, double ratio, long idleExecutorsCount) {
        return Jenkins.get().getQueue().getBuildableItems()
                .stream()
                .filter(b -> b.task.getEstimatedDuration() != -1L
                        && b.task.getEstimatedDuration() * ratio < longestExecutorTime)
                .map(b -> b.getId())
                .limit(idleExecutorsCount)
                .collect(Collectors.toList());
    }

    /**
     * Logs info about an item to be cancelled
     * @param item Queue.BuildableItem
     */
    private void logCancelTaskInfo(Queue.BuildableItem item) {
        logger.info("Canceling task " + item.task.getName() + " with an estimated duration "
                + item.task.getEstimatedDuration());
    }

    /**
     * @return array copy of BuildableItems
     */
    private Queue.BuildableItem[] getBuildablesCopy() {
        Collection<Queue.BuildableItem> buildables = Jenkins.get().getQueue().getBuildableItems();
        return buildables.toArray(new Queue.BuildableItem[buildables.size()]);
    }

    /**
     * logs info about items
     * @param items array of Queue.BuildableItem
     */
    private void logBuildablesCopy(Queue.BuildableItem[] items) {
        logger.info("BUILDABLE ITEMS COPY\n");
        for (Queue.BuildableItem item : items) {
            logger.info("Task name: " + item.task.getName() + " ID: " + item.getId());
        }

        //could use Arrays.stream()...
    }

    /**
     * Cancels quieting down, waits for timeMillis and then starts quieting down again
     * @param timeMillis "Open queue time" from the settings
     * @throws InterruptedException
     */
    @SuppressFBWarnings(value = {"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"}, justification = "intentional global state keepers")
    private void cancelAndDoQuietDown(long timeMillis) throws InterruptedException {
        if (jenkinsInstance.getQueue().getBuildableItems().size() > 0)
        {
            Utils.setCanAddToQueue(false);
            logger.warning("Canceling shutdown for " + timeMillis + " milliseconds.");
            jenkinsInstance.doCancelQuietDown();
            Thread.sleep(timeMillis);
            logger.warning("Start shutdown.");
            jenkinsInstance.doQuietDown();
        }

        Utils.setCanAddToQueue(false);
    }

    /**
     * schedules BuildableItems' tasks which are not in whiteListIDs and were cancelled before
     * @param buildablesCopy array copy of BuildableItems
     * @param whiteListIDs list of BuildableItems' IDs which are allowed to stay in the BuildableQueue
     */
    private void putTasksBackToQueueBut(Queue.BuildableItem[] buildablesCopy, List<Long> whiteListIDs) {
        for (Queue.BuildableItem item : buildablesCopy) {
            if (!whiteListIDs.contains(item.getId())) {
                logger.info("Adding " + item.task.getName() + " back to the queue.");
                jenkinsInstance.getQueue().schedule(item.task, 0);
            }
        }
    }
}
