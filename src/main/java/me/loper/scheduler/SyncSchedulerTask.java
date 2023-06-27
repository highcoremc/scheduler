package me.loper.scheduler;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SyncSchedulerTask<T> implements SchedulerTask<T> {

    private final Future<T> future;
    private final boolean repeatable;
    private final boolean isInMainThread;

    public SyncSchedulerTask(Future<T> future, boolean repeatable, boolean inMainThread) {
        this.future = future;
        this.repeatable = repeatable;
        this.isInMainThread = inMainThread;
    }

    @Override
    public boolean isRepeatable() {
        return this.repeatable;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public T await() {
        if (this.isInMainThread) {
            throw new RuntimeException("Await can't be called in the main thread, because it is blocks it.");
        }

        try {
            return this.future.get(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void cancel() {
        this.future.cancel(false);
    }
}
