package me.loper.scheduler;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SyncSchedulerTask<T> implements SchedulerTask<T> {

    private final Future<T> future;
    private final boolean repeatable;

    SyncSchedulerTask(Future<T> future) {
        this.future = future;
        this.repeatable = false;
    }

    SyncSchedulerTask(ScheduledFuture<T> future, boolean repeatable) {
        this.future = future;
        this.repeatable = repeatable;
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
    public T await() throws ExecutionException, InterruptedException, TimeoutException {
        return this.future.get(50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancel() {
        this.future.cancel(false);
    }
}
