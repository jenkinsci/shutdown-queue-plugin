package cz.muni.fi.xkozubi1;

import hudson.model.Queue;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Compares Queue.BuildableItems by getEstimatedDuration().
 * Used with ShutdownQueueSorter.
 * @author Dominik Kozubik
 */
public class EstimatedDurationComparator implements Comparator<Queue.BuildableItem>, Serializable {

    @Override
    public int compare(Queue.BuildableItem buildableItem0, Queue.BuildableItem buildableItem1) {

        if (buildableItem0.task.getEstimatedDuration() < buildableItem1.task.getEstimatedDuration()) {
            return -1;
        }

        if (buildableItem0.task.getEstimatedDuration() > buildableItem1.task.getEstimatedDuration()) {
            return 1;
        }

        return 0;
    }
}
