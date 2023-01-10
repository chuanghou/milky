package com.stellariver.milky.infrastructure.base.redis;

import com.stellariver.milky.common.tool.common.UK;
import com.stellariver.milky.common.tool.util.Collect;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import javax.annotation.Nullable;

/**
 * @author houchuang
 */
@CustomLog
@RequiredArgsConstructor
public class RedisHelper {

    private static final String SUCCESS_STATUS = "OK";

    private static final Long RELEASE_SUCCESS = 1L;

    final JedisPool jedisPool;

    @SuppressWarnings("unused")
    public boolean tryLock(UK nameSpace, String key, String encryption, long expire) {
        return tryLockFallbackable(nameSpace, key, encryption, expire, false);
    }

    public boolean tryLockFallbackable(UK nameSpace, String key, String encryption, long expire, boolean fallbackable) {
        key = nameSpace.getKey() + "_" + key;
        try (Jedis jedis = jedisPool.getResource()) {
            SetParams setParams = SetParams.setParams().nx().ex(expire);
            String result = jedis.set(key, encryption, setParams);
            return SUCCESS_STATUS.equals(result);
        } catch (Throwable e) {
            log.error("redis tryLock exception, key:{}, encryption:{}, expire:{}, fallbackable:{}",
                    key, encryption, expire, fallbackable, e);
            if (!fallbackable) {
                throw e;
            }
        }
        return false;
    }

    static final String SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @SuppressWarnings("unused")
    public boolean unlock(UK nameSpace, String key, String encryption) {
        return unlockFallbackable(nameSpace, key, encryption, false);
    }

    public boolean unlockFallbackable(UK nameSpace, String key, String encryption, boolean fallbackable) {
        key = nameSpace.getKey() + "_" + key;
        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.eval(SCRIPT, Collect.asList(key), Collect.asList(encryption));
            return RELEASE_SUCCESS.equals(result);
        } catch (Throwable e) {
            log.error("redis unlock exception, key:{}, encryption:{}, fallbackable:{}",
                    key, encryption, fallbackable, e);
            if (!fallbackable) {
                throw e;
            }
        }
        return false;
    }

    public boolean set(UK nameSpace, String key, String value, long expire) {
        return setFallbackable(nameSpace, key, value, expire, false);
    }

    public boolean setFallbackable(UK nameSpace, String key, String value, long expire, boolean fallbackable) {
        key = nameSpace.getKey() + "_" + key;
        try (Jedis jedis = jedisPool.getResource()) {
            SetParams setParams = SetParams.setParams().ex(expire);
            String result = jedis.set(key, value, setParams);
            return SUCCESS_STATUS.equals(result);
        } catch (Throwable t) {
            log.error("redis set exception, key:{}, value:{}, expire:{}, fallbackable:{}", key, value, expire, fallbackable, t);
            if (!fallbackable) {
                throw t;
            }
        }
        return false;
    }

    @Nullable
    @SuppressWarnings("unused")
    public String get(UK nameSpace, String key) {
        return getFallbackable(nameSpace, key, false);
    }

    @Nullable
    public String getFallbackable(UK nameSpace, String key, boolean fallbackable) {
        key = nameSpace.getKey() + "_" + key;
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Throwable e) {
            log.error("redis get exception, key:{}, fallbackable:{}", key, fallbackable, e);
            if (!fallbackable) {
                throw e;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public boolean delete(UK nameSpace, String key) {
        return deleteFallbackable(nameSpace, key, false);
    }

    public boolean deleteFallbackable(UK nameSpace, String key, boolean fallbackable) {
        key = nameSpace.getKey() + "_" + key;
        try (Jedis jedis = jedisPool.getResource()) {
            return Long.valueOf(1).equals(jedis.del(key));
        } catch (Throwable e) {
            log.error("redis delete exception, key:{}, fallbackable:{}", key, fallbackable, e);
            if (!fallbackable) {
                throw e;
            }
        }
        return false;
    }

}
