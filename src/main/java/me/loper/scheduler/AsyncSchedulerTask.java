package me.loper.scheduler;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncSchedulerTask<T> implements SchedulerTask<T> {

    private final Future<T> future;
    private final boolean repeatable;

    AsyncSchedulerTask(Future<T> future) {
        this.future = future;
        this.repeatable = false;
    }

    AsyncSchedulerTask(Future<T> future, boolean repeatable) {
        this.future = future;
        this.repeatable = repeatable;
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
    public T await() throws ExecutionException, InterruptedException {

        // check if this is main thread

        return this.future.get();
    }

    @Override
    public void cancel() {
        this.future.cancel(false);
    }
}
