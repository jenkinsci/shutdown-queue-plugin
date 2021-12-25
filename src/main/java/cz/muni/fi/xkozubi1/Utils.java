package cz.muni.fi.xkozubi1;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.queue.QueueSorter;
import jenkins.model.Jenkins;


/**
 * @author Dominik Kozubik
 */
public class Utils {


    private static boolean canAddToQueue = true;

    public static void setCanAddToQueue(boolean canAddToQueue) {
        Utils.canAddToQueue = canAddToQueue;
    }

    public static boolean isCanAddToQueue() {
        return canAddToQueue;
    }

    /**
     * Based on input condition, sets QueueSorter either to ShutdownQueueSorter or the original sorter.
     * @param isSorterOn boolean value from the settings option "Sorter on"
     */
    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"}, justification = "filled async, but may be correct")
    public static void handleSorterOn(boolean isSorterOn) {
        if (isSorterOn) {
            QueueSorter originalSorter = Jenkins.getInstanceOrNull().getQueue().getSorter();
            if (originalSorter == null) {
                originalSorter = new DefaultSorter();
            }
            Jenkins.getInstanceOrNull().getQueue().setSorter(new ShutdownQueueSorter(originalSorter));

        } else {
            doReset();
        }
    }

    /**
     * Sets QueueSorter to DefaultSorter if it was ShutdownQueueSorter before.
     */
    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"}, justification = "filled async, but may be correct")
    public static void doReset() {
        QueueSorter sorter = Jenkins.getInstanceOrNull().getQueue().getSorter();
        if (sorter instanceof ShutdownQueueSorter) {
            Jenkins.getInstanceOrNull().getQueue().setSorter(new DefaultSorter());
            ((ShutdownQueueSorter) sorter).reset();
        }
    }
}
