package cz.muni.fi.xkozubi1;

import hudson.model.Computer;
import hudson.model.Queue;
import jenkins.model.Jenkins;

import java.util.OptionalLong;


public class HandleQuietingDown {
    private final Computer computer;
    private final Jenkins jenkinsInstance;

    HandleQuietingDown(Computer computer) {
        this.computer = computer;
        this.jenkinsInstance = Jenkins.getInstanceOrNull();
    }

    public void handleLogic() throws InterruptedException {
//        long longestRemainingTime = ShutdownQueueConfiguration.getInstance().getMilliseconds() > 0 ?
//                ShutdownQueueConfiguration.getInstance().getMilliseconds() : getLongestExecutorRemainingTime();
        long longestRemainingTime = getLongestExecutorRemainingTime();

        System.out.println("Found longest remaining time: " + longestRemainingTime);

        if(longestRemainingTime != 0) {
            cancelTasksLongerThan(longestRemainingTime);
        }

        if (longestRemainingTime <= -1L) { // no free executor available
            System.out.println("No free executor available");
            return;
        }

        if (jenkinsInstance.getQueue().getBuildableItems().size() > 0)
        {
            System.out.println("Canceling quiet down");
            jenkinsInstance.doCancelQuietDown();
            Thread.sleep(500);
            System.out.println("Starting quieting down");
            jenkinsInstance.doQuietDown();
        }
    }

    private long getLongestExecutorRemainingTime() {
        OptionalLong maxRemainingTime = computer.getExecutors()
                .stream()
                .mapToLong(e -> e.getEstimatedRemainingTimeMillis())
                .max();

        return maxRemainingTime.isPresent() ? maxRemainingTime.getAsLong() : -1L;
    }
    
    private void cancelTasksLongerThan(long time) {
        Queue queue = Jenkins.getInstanceOrNull().getQueue();
        for(Queue.BuildableItem item : queue.getBuildableItems()) {
            System.out.println("estimated duration: " + item.task.getEstimatedDuration() + " time: "  + time);
            if (item.task.getEstimatedDuration() > time) {
                queue.cancel(item.task);
            }
        }
    }
}
