package me.loper.scheduler;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncSchedulerTask<T> implements SchedulerTask<T> {

    private final Future<T> future;
    private final boolean repeatable;
    private final boolean isInMainThread;

    AsyncSchedulerTask(Future<T> future, boolean repeatable, boolean isInMainThread) {
        this.future = future;
        this.repeatable = repeatable;
        this.isInMainThread = isInMainThread;
    }

    @Override
    public boolean isRepeatable() {
        return this.repeatable;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public T await() {
        if (this.isInMainThread) {
            throw new RuntimeException("Await can't be called in the main thread, because it is blocks it.");
        }

        try {
            return this.future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancel() {
        this.future.cancel(false);
    }
}
