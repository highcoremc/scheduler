package me.loper.scheduler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface SchedulerTask <T> {

    boolean isRepeatable();

    boolean isAsync();

    T await() throws ExecutionException, InterruptedException, TimeoutException;

    /**
     * Cancels the task.
     */
    void cancel();
}
