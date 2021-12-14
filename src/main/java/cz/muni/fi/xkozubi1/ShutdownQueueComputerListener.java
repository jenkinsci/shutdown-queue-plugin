package cz.muni.fi.xkozubi1;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * Plugin initiation
 * @Author Dominik Kozubik
 */
@Extension
public class ShutdownQueueComputerListener extends ComputerListener {
    private static Logger logger = Logger.getLogger(ShutdownQueueConfiguration.class.getName());
    private static ScheduledExecutorService executorService;
    private static ShutdownTask shutdownTask;
    private static ScheduledFuture<?> futureTask;

    @Override
    public void onOnline(Computer c, TaskListener listener) {
        onTemporarilyOnline(c);
    }

    @Override
    @SuppressFBWarnings(value = {"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"}, justification = "intentional global state keepers")
    public void onTemporarilyOnline(Computer computer) {
        executorService = Executors.newSingleThreadScheduledExecutor();
        shutdownTask = new ShutdownTask(computer);
        changeScheduleInterval(ShutdownQueueConfiguration.getInstance().getPeriodRunnable());

        logger.info("Shutdown-queue plugin thread has started.");
    }

    /**
     * Changes delay value of a periodic callable shutdownTask.
     * @param time seconds
     */
    static synchronized  void changeScheduleInterval(long time)
    {
        if(time > 0)
        {
            if (futureTask != null)
            {
                futureTask.cancel(false);
            }

            futureTask = executorService.scheduleAtFixedRate(shutdownTask, 1, time, TimeUnit.SECONDS);
        }
    }
}
