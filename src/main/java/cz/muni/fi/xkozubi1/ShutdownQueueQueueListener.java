package cz.muni.fi.xkozubi1;

import hudson.model.Queue;
import hudson.model.queue.QueueListener;


public class ShutdownQueueQueueListener extends QueueListener {
    @Override
    public void onEnterWaiting(Queue.WaitingItem wi) {
        if (Utils.canAddToQueue) {
            super.onEnterWaiting(wi);
        }
    }
}
