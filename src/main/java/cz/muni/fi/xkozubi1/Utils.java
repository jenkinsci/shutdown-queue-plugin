package cz.muni.fi.xkozubi1;

import hudson.model.queue.QueueSorter;
import jenkins.model.Jenkins;


/**
 * @Author Dominik Kozubik
 */
public class Utils {

    public static boolean canAddToQueue = true;

    /**
     * Based on input condition, sets QueueSorter either to ShutdownQueueSorter or the original sorter.
     * @param isSorterOn boolean value from the settings option "Sorter on"
     */
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
    public static void doReset() {
        QueueSorter sorter = Jenkins.getInstanceOrNull().getQueue().getSorter();
        if (sorter instanceof ShutdownQueueSorter) {
            Jenkins.getInstanceOrNull().getQueue().setSorter(new DefaultSorter());
            ((ShutdownQueueSorter) sorter).reset();
        }
    }
}
