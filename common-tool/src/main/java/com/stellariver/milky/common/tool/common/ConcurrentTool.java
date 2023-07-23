package com.stellariver.milky.common.tool.common;

import com.stellariver.milky.common.tool.util.Collect;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author houchuang
 */
public class ConcurrentTool {

    @SneakyThrows({ExecutionException.class, InterruptedException.class})
    static public <P, V> Map<P, V> batchCall(Set<P> params, Function<P, V> function, Executor executor) {
        List<CompletableFuture<Pair<P, V>>> batchFutures = params.stream()
                .map(param -> CompletableFuture.supplyAsync(() -> Pair.of(param, function.apply(param)), executor))
                .collect(Collectors.toList());
        CompletableFuture<List<Pair<P, V>>> result = CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> batchFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        return Collect.toMap(result.get(), Pair::getKey, Pair::getValue);
    }

    @SneakyThrows({ExecutionException.class, InterruptedException.class})
    static public <P, V> Map<P, V> batchCall(List<P> params, Function<P, V> function, Executor executor) {
        List<CompletableFuture<Pair<P, V>>> batchFutures = params.stream()
                .map(param -> CompletableFuture.supplyAsync(() -> Pair.of(param, function.apply(param)), executor))
                .collect(Collectors.toList());
        CompletableFuture<List<Pair<P, V>>> result = CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> batchFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        return Collect.toMap(result.get(), Pair::getKey, Pair::getValue);
    }

    static public <P, V> Future<Map<P, V>> batchCallFuture(Set<P> params, Function<P, V> function, Executor executor) {
        List<CompletableFuture<Pair<P, V>>> batchFutures = params.stream()
                .map(param -> CompletableFuture.supplyAsync(() -> Pair.of(param, function.apply(param)), executor))
                .collect(Collectors.toList());
        return CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> batchFutures.stream().map(CompletableFuture::join))
                .thenApply(s -> Collect.toMap(s.collect(Collectors.toList()), Pair::getKey, Pair::getValue));
    }

    static public <P, V> Future<Map<P, V>> batchCallFuture(List<P> params, Function<P, V> function, Executor executor) {
        List<CompletableFuture<Pair<P, V>>> batchFutures = params.stream()
                .map(param -> CompletableFuture.supplyAsync(() -> Pair.of(param, function.apply(param)), executor))
                .collect(Collectors.toList());
        return CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> batchFutures.stream().map(CompletableFuture::join))
                .thenApply(s -> Collect.toMap(s.collect(Collectors.toList()), Pair::getKey, Pair::getValue));
    }

    @SneakyThrows({ExecutionException.class, InterruptedException.class})
    static public void batchRun(List<Runnable> runnables, Executor executor) {
        List<CompletableFuture<Void>> batchFuture = runnables.stream()
                .map(runnable -> CompletableFuture.runAsync(runnable, executor))
                .collect(Collectors.toList());
        CompletableFuture.allOf(batchFuture.toArray(new CompletableFuture[0]))
                .thenApply(v -> batchFuture.stream().map(CompletableFuture::join).collect(Collectors.toList())).get();
    }

    static public Future<Object> batchRunFuture(List<Runnable> runnables, Executor executor) {
        List<CompletableFuture<Void>> batchFuture = runnables.stream()
                .map(runnable -> CompletableFuture.runAsync(runnable, executor))
                .collect(Collectors.toList());
        return CompletableFuture.allOf(batchFuture.toArray(new CompletableFuture[0])).thenApply(v -> batchFuture.stream().map(CompletableFuture::join));
    }

}
