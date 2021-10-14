package cz.muni.fi.xkozubi1;

import hudson.model.Computer;
import jenkins.model.Jenkins;


public class ShutdownTask implements Runnable {

    private HandleQuietingDown handleQuietingDown;

    public ShutdownTask(Computer computer) {
        handleQuietingDown = new HandleQuietingDown(computer);
    }

    @Override
    public void run() {
        System.out.println("Run");
        if (ShutdownQueueConfiguration.getInstance().getCheckboxPlugin() && Jenkins.get().isQuietingDown()
                && Jenkins.get().getQueue().getBuildableItems().size() > 0) {
            try {
                handleQuietingDown.handleLogic();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
