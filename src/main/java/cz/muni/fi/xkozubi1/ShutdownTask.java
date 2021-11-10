package cz.muni.fi.xkozubi1;

import hudson.model.Computer;
import jenkins.model.Jenkins;


/**
 * Plugin runnable.
 * If the plugin is on, Jenkins is going to shut down, and the buildable queue is not empty, calls a method
 * handleLogic() from HandleQuietingDown which takes care of the rest of the plugin's logic.
 *
 * @Author Dominik Kozubik
 */
public class ShutdownTask implements Runnable {
    private HandleQuietingDown handleQuietingDown;

    public ShutdownTask(Computer computer) {
        handleQuietingDown = new HandleQuietingDown(computer);
    }

    @Override
    public void run() {
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
