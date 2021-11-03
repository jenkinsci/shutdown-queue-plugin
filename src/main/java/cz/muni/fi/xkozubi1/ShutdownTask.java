package cz.muni.fi.xkozubi1;

import hudson.model.Computer;
import jenkins.model.Jenkins;

import java.util.logging.Logger;


public class ShutdownTask implements Runnable {
    private static Logger logger = Logger.getLogger(ShutdownTask.class.getName());
    private HandleQuietingDown handleQuietingDown;

    public ShutdownTask(Computer computer) {
        handleQuietingDown = new HandleQuietingDown(computer);
    }

    @Override
    public void run() {
        logger.info("Run");
        if (ShutdownQueueConfiguration.getInstance().getPluginOn() &&
                Jenkins.get().isQuietingDown() &&
                Jenkins.get().getQueue().getBuildableItems().size() > 0) {
            try {
                handleQuietingDown.handleLogic();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
