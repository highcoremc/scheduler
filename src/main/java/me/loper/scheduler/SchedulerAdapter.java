package me.loper.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
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
    }

    /**
     * Executes a task sync with result
     *
     * @param task the task
     */
    default <V> SchedulerTask<V> executeSync(Callable<V> task) {
        return new SyncSchedulerTask<>(futureFromCallable(sync(task)), false, this.isInMainThread());
    }

    /**
     * Executes a task async with result
     *
     * @param task the task
     */
    default <V> SchedulerTask<V> executeAsync(Callable<V> task) {
        return new AsyncSchedulerTask<>(futureFromCallable(async(task)), false, this.isInMainThread());
    }

    /**
     * Executes a task sync without result
     *
     * @param task the task
     */
    default SchedulerTask<?> executeSync(Runnable task) {
        return new SyncSchedulerTask<>(futureFromCallable(sync(task)), false, this.isInMainThread());
    }

    /**
     * Executes a task async without result
     *
     * @param task the task
     */
    default SchedulerTask<?> executeAsync(Runnable task) {
        return new AsyncSchedulerTask<>(futureFromCallable(async(task)), false, this.isInMainThread());
    }

    static <V> Future<V> futureFromCallable(Callable<Future<V>> callable) {
        Future<V> future;

        try {
            future = callable.call();
        } catch (Exception ex) {
            CompletableFuture<V> completableFuture = new CompletableFuture<>();
            completableFuture.completeExceptionally(ex);

            future = completableFuture;
        }

        return future;
    }

    /**
     * Executes the given task with a delay and returns result.
     *
     * @param task  the task
     * @param delay the delay
     * @param unit  the unit of delay
     * @return the resultant task instance
     */
    <V> SchedulerTask<V> asyncLater(Callable<V> task, long delay, TimeUnit unit);

    <V> SchedulerTask<V> syncLater(Callable<V> task, long delay, TimeUnit unit);

    /**
     * Executes the given task with a delay without result.
     *
     * @param task  the task
     * @param delay the delay
     * @param unit  the unit of delay
     * @return the resultant task instance
     */
    SchedulerTask<?> asyncLater(Runnable task, long delay, TimeUnit unit);

    SchedulerTask<?> syncLater(Runnable task, long delay, TimeUnit unit);

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
