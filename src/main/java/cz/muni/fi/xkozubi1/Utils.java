package cz.muni.fi.xkozubi1;

import hudson.model.queue.QueueSorter;
import jenkins.model.Jenkins;

public class Utils {

    static void handleSorterOn(boolean isSorterOn) {
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

    static void doReset() {
        QueueSorter sorter = Jenkins.getInstanceOrNull().getQueue().getSorter();
        if (sorter instanceof ShutdownQueueSorter) {
            Jenkins.getInstanceOrNull().getQueue().setSorter(new DefaultSorter());
            ((ShutdownQueueSorter) sorter).reset();
        }
    }
}
