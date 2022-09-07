package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.util.Collect;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConcurrentTool {

    static public <P, V> Map<P, V> batchCall(Set<P> params, Function<P, V> function, Executor executor) {
        List<CompletableFuture<Pair<P, V>>> batchFutures = params.stream()
                .map(param -> CompletableFuture.supplyAsync(() -> Pair.of(param, function.apply(param)), executor))
                .collect(Collectors.toList());
        CompletableFuture<Void> result = CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<Pair<P, V>>> finalResults = result.thenApply(v -> batchFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        try {
            List<Pair<P, V>> pairs = finalResults.get();
            return Collect.toMap(pairs, Pair::getKey, Pair::getValue);
        } catch (InterruptedException e) {
            throw new SysException(e);
        } catch (ExecutionException e) {
            throw new SysException(e.getCause());
        }
    }

    static public void batchRun(List<Runnable> runnables, Executor executor) {
        List<CompletableFuture<Void>> batchFuture = runnables.stream()
                .map(runnable -> CompletableFuture.runAsync(runnable, executor))
                .collect(Collectors.toList());
        CompletableFuture<Void> result = CompletableFuture.allOf(batchFuture.toArray(new CompletableFuture[0]));
        CompletableFuture<List<Void>> finalResults =
                result.thenApply(v -> batchFuture.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        try {
            finalResults.get();
        } catch (InterruptedException e) {
            throw new SysException(e);
        } catch (ExecutionException e) {
            throw new SysException(e.getCause());
        }
    }
}
