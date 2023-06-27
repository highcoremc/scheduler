package me.loper.scheduler;

public interface SchedulerTask <T> {

    boolean isRepeatable();

    boolean isAsync();

    T await();

    T await(boolean force);

    /**
     * Cancels the task.
     */
    void cancel();
}
