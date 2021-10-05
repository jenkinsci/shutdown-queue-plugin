package cz.muni.fi.xkozubi1;

import hudson.model.Queue;
import hudson.model.queue.AbstractQueueSorterImpl;

import java.io.Serializable;
import java.util.List;

public class DefaultSorter extends AbstractQueueSorterImpl implements Serializable {

    @Override
    public void sortBuildableItems(List<Queue.BuildableItem> list) {
        list.sort(this);
    }
}
