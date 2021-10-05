package cz.muni.fi.xkozubi1;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import jenkins.model.Jenkins;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Extension
public class ShutdownQueueComputerListener extends ComputerListener {

    @Override
    public void onOnline(Computer c, TaskListener listener) {
        onTemporarilyOnline(c);
    }

    @Override
    public void onTemporarilyOnline(Computer computer) {
        HandleQuietingDown logicClass = new HandleQuietingDown(computer);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(() -> {
            System.out.println("Run");
            if (ShutdownQueueConfiguration.getInstance().getCheckboxPlugin() && Jenkins.get().isQuietingDown()
                    && !Jenkins.get().getQueue().isEmpty()) {
                logicClass.handleLogic();
            }
        }, 1, 1, TimeUnit.SECONDS);

        System.out.println("Thread started");
    }
}
