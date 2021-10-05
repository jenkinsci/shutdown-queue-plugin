package cz.muni.fi.xkozubi1;

import jenkins.model.Jenkins;

import java.util.Objects;

public class ShutdownQueueRunnable implements Runnable {

    private HandleQuietingDown logicClass;

    public ShutdownQueueRunnable(String computerName) {
        logicClass = new HandleQuietingDown(Objects.requireNonNull(Jenkins.getInstanceOrNull())
                .getComputer(computerName));
    }

    @Override
    public void run() {

        System.out.println("Runnable run");
        if (ShutdownQueueConfiguration.getInstance().getCheckboxPlugin() && Jenkins.get().isQuietingDown()
                && !Jenkins.get().getQueue().isEmpty()) {
            logicClass.handleLogic();
        }
    }
}
