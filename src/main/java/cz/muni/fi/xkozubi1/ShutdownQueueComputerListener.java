package cz.muni.fi.xkozubi1;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Extension
public class ShutdownQueueComputerListener extends ComputerListener {

    private static ScheduledExecutorService executorService;
    private static ShutdownTask shutdownTask;
    private static ScheduledFuture<?> futureTask;

    @Override
    public void onOnline(Computer c, TaskListener listener) {
        onTemporarilyOnline(c);
    }

    @Override
    public void onTemporarilyOnline(Computer computer) {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.shutdownTask = new ShutdownTask(computer);
        changeReadInterval(ShutdownQueueConfiguration.getInstance().getPeriodRunnable());

        System.out.println("Thread started");
    }

    public static void changeReadInterval(long time)
    {
        if(time > 0)
        {
            if (futureTask != null)
            {
                futureTask.cancel(true);
            }

            futureTask = executorService.scheduleAtFixedRate(shutdownTask, 1, time, TimeUnit.SECONDS);
        }
    }
}
