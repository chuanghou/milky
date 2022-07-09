package com.stellariver.milky.common.tool.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConcurrentTool {

    static public <T> List<T> batchCall(List<Supplier<T>> suppliers, Executor executor) {
        List<CompletableFuture<T>> batchFutures = suppliers.stream()
                .map(supplier -> CompletableFuture.supplyAsync(supplier, executor))
                .collect(Collectors.toList());
        CompletableFuture<Void> result = CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<T>> finalResults =
                result.thenApply(v -> batchFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        try {
            return finalResults.get();
        } catch (InterruptedException e) {
            throw new SysException(e);
        } catch (ExecutionException e) {
            throw new SysException(e.getCause());
        }
    }

    static public void batchRun(List<Runnable> runnables, Executor executor) {
        List<CompletableFuture<Void>> batchFutures = runnables.stream()
                .map(runnable -> CompletableFuture.runAsync(runnable, executor))
                .collect(Collectors.toList());
        CompletableFuture<Void> result = CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<Void>> finalResults =
                result.thenApply(v -> batchFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        try {
            finalResults.get();
        } catch (InterruptedException e) {
            throw new SysException(e);
        } catch (ExecutionException e) {
            throw new SysException(e.getCause());
        }
    }
}
