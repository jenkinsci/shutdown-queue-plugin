package cz.muni.fi.xkozubi1;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"}, justification = "filled async, but may be correct")
    void reset() {
        sortBuildableItems(Jenkins.getInstanceOrNull().getQueue().getBuildableItems());
        Queue.getInstance().maintain();
    }
}
