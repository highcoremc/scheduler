package me.loper.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A scheduler for running tasks using the systems provided by the platform
 */
public interface SchedulerAdapter {

    /**
     * Gets an async executor instance
     */
    <V> Callable<Future<V>> async(Callable<V> callable);

    boolean isInMainThread();

    /**
     * Gets a sync executor instance
     */
    <V> Callable<Future<V>> sync(Callable<V> callable);

    default Callable<Future<Object>> async(Runnable runnable) {
        return this.async(Executors.callable(runnable));
    }

    default Callable<Future<Object>> sync(Runnable runnable) {
        return this.sync(Executors.callable(runnable));
    };

    /**
     * Executes a task async
     *
     * @param task the task
     */
    default <V> SchedulerTask<V> executeAsync(Callable<V> task) throws Exception {
        return new AsyncSchedulerTask<>(async(task).call(), false, this.isInMainThread());
    }

    /**
     * Executes a task sync
     *
     * @param task the task
     */
    default <V> SchedulerTask<V> executeSync(Callable<V> task) throws Exception {
        return new SyncSchedulerTask<>(sync(task).call(), false, this.isInMainThread());
    }

    /**
     * Executes the given task with a delay.
     *
     * @param task  the task
     * @param delay the delay
     * @param unit  the unit of delay
     * @return the resultant task instance
     */
    <V> SchedulerTask<V> asyncLater(Callable<V> task, long delay, TimeUnit unit);

    <V> SchedulerTask<V> syncLater(Callable<V> task, long delay, TimeUnit unit);

    /**
     * Executes the given task repeatedly async at a given interval.
     *
     * @param task     the task
     * @param interval the interval
     * @param unit     the unit of interval
     * @return the resultant task instance
     */
    SchedulerTask<?> asyncRepeating(Runnable task, long interval, TimeUnit unit);

    SchedulerTask<?> syncRepeating(Runnable task, long interval, TimeUnit unit);

    /**
     * Shuts down the scheduler instance.
     */
    void shutdown();

}
