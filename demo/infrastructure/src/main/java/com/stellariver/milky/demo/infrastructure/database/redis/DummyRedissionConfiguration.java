package com.stellariver.milky.demo.infrastructure.database.redis;

import com.stellariver.milky.common.base.Result;
import com.stellariver.milky.domain.support.dependency.ConcurrentOperate;
import com.stellariver.milky.spring.partner.WindowsCondition;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.*;
import org.redisson.api.redisnode.BaseRedisNodes;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@Conditional(WindowsCondition.class)
public class DummyRedissionConfiguration {

    @Bean
    RedissonClient redissonClient() {
        return new RedissonClient() {
            @Override
            public <V, L> RTimeSeries<V, L> getTimeSeries(String name) {
                return null;
            }

            @Override
            public <V, L> RTimeSeries<V, L> getTimeSeries(String name, Codec codec) {
                return null;
            }

            @Override
            public <K, V> RStream<K, V> getStream(String name) {
                return null;
            }

            @Override
            public <K, V> RStream<K, V> getStream(String name, Codec codec) {
                return null;
            }

            @Override
            public RRateLimiter getRateLimiter(String name) {
                return null;
            }

            @Override
            public RBinaryStream getBinaryStream(String name) {
                return null;
            }

            @Override
            public <V> RGeo<V> getGeo(String name) {
                return null;
            }

            @Override
            public <V> RGeo<V> getGeo(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RSetCache<V> getSetCache(String name) {
                return null;
            }

            @Override
            public <V> RSetCache<V> getSetCache(String name, Codec codec) {
                return null;
            }

            @Override
            public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec) {
                return null;
            }

            @Override
            public <K, V> RMapCache<K, V> getMapCache(String name, Codec codec, MapOptions<K, V> options) {
                return null;
            }

            @Override
            public <K, V> RMapCache<K, V> getMapCache(String name) {
                return null;
            }

            @Override
            public <K, V> RMapCache<K, V> getMapCache(String name, MapOptions<K, V> options) {
                return null;
            }

            @Override
            public <V> RBucket<V> getBucket(String name) {
                return null;
            }

            @Override
            public <V> RBucket<V> getBucket(String name, Codec codec) {
                return null;
            }

            @Override
            public RBuckets getBuckets() {
                return null;
            }

            @Override
            public RBuckets getBuckets(Codec codec) {
                return null;
            }

            @Override
            public <V> RJsonBucket<V> getJsonBucket(String name, JsonCodec<V> codec) {
                return null;
            }

            @Override
            public <V> RHyperLogLog<V> getHyperLogLog(String name) {
                return null;
            }

            @Override
            public <V> RHyperLogLog<V> getHyperLogLog(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RList<V> getList(String name) {
                return null;
            }

            @Override
            public <V> RList<V> getList(String name, Codec codec) {
                return null;
            }

            @Override
            public <K, V> RListMultimap<K, V> getListMultimap(String name) {
                return null;
            }

            @Override
            public <K, V> RListMultimap<K, V> getListMultimap(String name, Codec codec) {
                return null;
            }

            @Override
            public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name) {
                return null;
            }

            @Override
            public <K, V> RListMultimapCache<K, V> getListMultimapCache(String name, Codec codec) {
                return null;
            }

            @Override
            public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, LocalCachedMapOptions<K, V> options) {
                return null;
            }

            @Override
            public <K, V> RLocalCachedMap<K, V> getLocalCachedMap(String name, Codec codec, LocalCachedMapOptions<K, V> options) {
                return null;
            }

            @Override
            public <K, V> RMap<K, V> getMap(String name) {
                return null;
            }

            @Override
            public <K, V> RMap<K, V> getMap(String name, MapOptions<K, V> options) {
                return null;
            }

            @Override
            public <K, V> RMap<K, V> getMap(String name, Codec codec) {
                return null;
            }

            @Override
            public <K, V> RMap<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
                return null;
            }

            @Override
            public <K, V> RSetMultimap<K, V> getSetMultimap(String name) {
                return null;
            }

            @Override
            public <K, V> RSetMultimap<K, V> getSetMultimap(String name, Codec codec) {
                return null;
            }

            @Override
            public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name) {
                return null;
            }

            @Override
            public <K, V> RSetMultimapCache<K, V> getSetMultimapCache(String name, Codec codec) {
                return null;
            }

            @Override
            public RSemaphore getSemaphore(String name) {
                return null;
            }

            @Override
            public RPermitExpirableSemaphore getPermitExpirableSemaphore(String name) {
                return null;
            }

            @Override
            public RLock getLock(String name) {
                return null;
            }

            @Override
            public RLock getSpinLock(String name) {
                return null;
            }

            @Override
            public RLock getSpinLock(String name, LockOptions.BackOff backOff) {
                return null;
            }

            @Override
            public RFencedLock getFencedLock(String name) {
                return null;
            }

            @Override
            public RLock getMultiLock(RLock... locks) {
                return null;
            }

            @Override
            public RLock getRedLock(RLock... locks) {
                return null;
            }

            @Override
            public RLock getFairLock(String name) {
                return null;
            }

            @Override
            public RReadWriteLock getReadWriteLock(String name) {
                return null;
            }

            @Override
            public <V> RSet<V> getSet(String name) {
                return null;
            }

            @Override
            public <V> RSet<V> getSet(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RSortedSet<V> getSortedSet(String name) {
                return null;
            }

            @Override
            public <V> RSortedSet<V> getSortedSet(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RScoredSortedSet<V> getScoredSortedSet(String name) {
                return null;
            }

            @Override
            public <V> RScoredSortedSet<V> getScoredSortedSet(String name, Codec codec) {
                return null;
            }

            @Override
            public RLexSortedSet getLexSortedSet(String name) {
                return null;
            }

            @Override
            public RShardedTopic getShardedTopic(String name) {
                return null;
            }

            @Override
            public RShardedTopic getShardedTopic(String name, Codec codec) {
                return null;
            }

            @Override
            public RTopic getTopic(String name) {
                return null;
            }

            @Override
            public RTopic getTopic(String name, Codec codec) {
                return null;
            }

            @Override
            public RReliableTopic getReliableTopic(String name) {
                return null;
            }

            @Override
            public RReliableTopic getReliableTopic(String name, Codec codec) {
                return null;
            }

            @Override
            public RPatternTopic getPatternTopic(String pattern) {
                return null;
            }

            @Override
            public RPatternTopic getPatternTopic(String pattern, Codec codec) {
                return null;
            }

            @Override
            public <V> RQueue<V> getQueue(String name) {
                return null;
            }

            @Override
            public <V> RTransferQueue<V> getTransferQueue(String name) {
                return null;
            }

            @Override
            public <V> RTransferQueue<V> getTransferQueue(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RDelayedQueue<V> getDelayedQueue(RQueue<V> destinationQueue) {
                return null;
            }

            @Override
            public <V> RQueue<V> getQueue(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RRingBuffer<V> getRingBuffer(String name) {
                return null;
            }

            @Override
            public <V> RRingBuffer<V> getRingBuffer(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RPriorityQueue<V> getPriorityQueue(String name) {
                return null;
            }

            @Override
            public <V> RPriorityQueue<V> getPriorityQueue(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name) {
                return null;
            }

            @Override
            public <V> RPriorityBlockingQueue<V> getPriorityBlockingQueue(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name) {
                return null;
            }

            @Override
            public <V> RPriorityBlockingDeque<V> getPriorityBlockingDeque(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RPriorityDeque<V> getPriorityDeque(String name) {
                return null;
            }

            @Override
            public <V> RPriorityDeque<V> getPriorityDeque(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RBlockingQueue<V> getBlockingQueue(String name) {
                return null;
            }

            @Override
            public <V> RBlockingQueue<V> getBlockingQueue(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name) {
                return null;
            }

            @Override
            public <V> RBoundedBlockingQueue<V> getBoundedBlockingQueue(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RDeque<V> getDeque(String name) {
                return null;
            }

            @Override
            public <V> RDeque<V> getDeque(String name, Codec codec) {
                return null;
            }

            @Override
            public <V> RBlockingDeque<V> getBlockingDeque(String name) {
                return null;
            }

            @Override
            public <V> RBlockingDeque<V> getBlockingDeque(String name, Codec codec) {
                return null;
            }

            @Override
            public RAtomicLong getAtomicLong(String name) {
                return null;
            }

            @Override
            public RAtomicDouble getAtomicDouble(String name) {
                return null;
            }

            @Override
            public RLongAdder getLongAdder(String name) {
                return null;
            }

            @Override
            public RDoubleAdder getDoubleAdder(String name) {
                return null;
            }

            @Override
            public RCountDownLatch getCountDownLatch(String name) {
                return null;
            }

            @Override
            public RBitSet getBitSet(String name) {
                return null;
            }

            @Override
            public <V> RBloomFilter<V> getBloomFilter(String name) {
                return null;
            }

            @Override
            public <V> RBloomFilter<V> getBloomFilter(String name, Codec codec) {
                return null;
            }

            @Override
            public RIdGenerator getIdGenerator(String name) {
                return null;
            }

            @Override
            public RFunction getFunction() {
                return null;
            }

            @Override
            public RFunction getFunction(Codec codec) {
                return null;
            }

            @Override
            public RScript getScript() {
                return null;
            }

            @Override
            public RScript getScript(Codec codec) {
                return null;
            }

            @Override
            public RScheduledExecutorService getExecutorService(String name) {
                return null;
            }

            @Override
            public RScheduledExecutorService getExecutorService(String name, ExecutorOptions options) {
                return null;
            }

            @Override
            public RScheduledExecutorService getExecutorService(String name, Codec codec) {
                return null;
            }

            @Override
            public RScheduledExecutorService getExecutorService(String name, Codec codec, ExecutorOptions options) {
                return null;
            }

            @Override
            public RRemoteService getRemoteService() {
                return null;
            }

            @Override
            public RRemoteService getRemoteService(Codec codec) {
                return null;
            }

            @Override
            public RRemoteService getRemoteService(String name) {
                return null;
            }

            @Override
            public RRemoteService getRemoteService(String name, Codec codec) {
                return null;
            }

            @Override
            public RTransaction createTransaction(TransactionOptions options) {
                return null;
            }

            @Override
            public RBatch createBatch(BatchOptions options) {
                return null;
            }

            @Override
            public RBatch createBatch() {
                return null;
            }

            @Override
            public RKeys getKeys() {
                return null;
            }

            @Override
            public RLiveObjectService getLiveObjectService() {
                return null;
            }

            @Override
            public RedissonRxClient rxJava() {
                return null;
            }

            @Override
            public RedissonReactiveClient reactive() {
                return null;
            }

            @Override
            public void shutdown() {

            }

            @Override
            public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {

            }

            @Override
            public Config getConfig() {
                return null;
            }

            @Override
            public <T extends BaseRedisNodes> T getRedisNodes(RedisNodes<T> nodes) {
                return null;
            }

            @Override
            public NodesGroup<Node> getNodesGroup() {
                return null;
            }

            @Override
            public ClusterNodesGroup getClusterNodesGroup() {
                return null;
            }

            @Override
            public boolean isShutdown() {
                return false;
            }

            @Override
            public boolean isShuttingDown() {
                return false;
            }

            @Override
            public String getId() {
                return null;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ConcurrentOperate concurrentOperate() {
        return new ConcurrentOperate() {
            @Override
            protected Map<String, Result<Void>> batchTryLock(List<Pair<String, Duration>> lockParams) {
                return lockParams.stream().collect(Collectors.toMap(Pair::getKey, s -> Result.success()));
            }

            @Override
            protected Map<String, Result<Void>> batchUnLock(Set<String> unlockIds) {
                return unlockIds.stream().collect(Collectors.toMap(Function.identity(), s -> Result.success()));
            }
        };

    }

}
