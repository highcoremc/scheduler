package me.loper.scheduler;

import me.loper.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Predicate;

import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Abstract implementation of {@link SchedulerAdapter} using a {@link ScheduledExecutorService}.
 */
public abstract class AbstractJavaScheduler implements SchedulerAdapter {
    private static final int PARALLELISM = 16;

    private final ScheduledThreadPoolExecutor scheduler;
    private final Logger logger;
    private final String schedulerName;

    public AbstractJavaScheduler(String schedulerName, Logger logger) {
        this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName(schedulerName);
            return thread;
        });
        this.logger = logger;
        this.schedulerName = schedulerName;
        this.scheduler.setRemoveOnCancelPolicy(true);
        this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    @Override
    public <V> SchedulerTask<V> asyncLater(Callable<V> task, long delay, TimeUnit unit) {
        ScheduledFuture<V> future = this.scheduler.schedule(() -> this.async(task).call().get(), delay, unit);

        return new AsyncSchedulerTask<>(future);
    }

    @Override
    public <V> SchedulerTask<V> syncLater(Callable<V> task, long delay, TimeUnit unit) {
        ScheduledFuture<V> future = this.scheduler.schedule(
                () -> this.sync(task).call().get(), delay, unit);

        return new SyncSchedulerTask<>(future);
    }

    @Override
    public SchedulerTask<?> asyncRepeating(Runnable task, long interval, TimeUnit unit) {
        return new AsyncSchedulerTask<>(scheduleRepeating(interval, unit, this.sync(task)), true);
    }

    @Override
    public SchedulerTask<?> syncRepeating(Runnable task, long interval, TimeUnit unit) {
        return new SyncSchedulerTask<>(scheduleRepeating(interval, unit, this.async(task)), true);
    }

    @NotNull
    private ScheduledFuture<?> scheduleRepeating(long interval, TimeUnit unit, Callable<Future<Object>> callable) {
        return this.scheduler.scheduleAtFixedRate(() -> {
            try {
                callable.call().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, interval, interval, unit);
    }

    @Override
    public void shutdown() {
        this.scheduler.shutdown();
        try {
            if (!this.scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                reportRunningTasks(thread -> thread.getName().equals(this.schedulerName));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void reportRunningTasks(Predicate<Thread> predicate) {
        Thread.getAllStackTraces().forEach((thread, stack) -> {
            if (predicate.test(thread)) {
                this.logger.warn("Thread " + thread.getName() + " is blocked, and may be the reason for the slow shutdown!\n" +
                        Arrays.stream(stack).map(el -> "  " + el).collect(Collectors.joining("\n"))
                );
            }
        });
    }
}