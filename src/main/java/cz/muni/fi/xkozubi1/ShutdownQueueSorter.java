package cz.muni.fi.xkozubi1;

import hudson.model.Queue;
import hudson.model.queue.QueueSorter;
import jenkins.model.Jenkins;

import java.util.Collections;
import java.util.List;


/**
 * Plugin's sorter. It is used if sorterOn or strategy type "Sort and remove longer" is on.
 * Also preserves default Jenkins sorter.
 */
public class ShutdownQueueSorter extends QueueSorter {

    private final QueueSorter originalQueueSorter;
    private final EstimatedDurationComparator comparator;

    public ShutdownQueueSorter(QueueSorter sorter) {
        originalQueueSorter = sorter;
        comparator = new EstimatedDurationComparator();
    }

    @Override
    public void sortBuildableItems(List<Queue.BuildableItem> buildableItems) {
        if (originalQueueSorter != null) {
            originalQueueSorter.sortBuildableItems(buildableItems);
        }

        Collections.sort(buildableItems, comparator);
    }

    void reset() {
        sortBuildableItems(Jenkins.getInstanceOrNull().getQueue().getBuildableItems());
        Queue.getInstance().maintain();
    }
}
