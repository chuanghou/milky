package com.stellariver.milky.infrastructure.base.redis;

import com.stellariver.milky.common.tool.common.NameSpace;
import com.stellariver.milky.common.tool.log.Logger;
import com.stellariver.milky.common.tool.util.Collect;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import javax.annotation.Nullable;

@CustomLog
@RequiredArgsConstructor
public class RedisHelper {

    private static final String SUCCESS_STATUS = "OK";

    private static final Long RELEASE_SUCCESS = 1L;

    final JedisPool jedisPool;

    @SuppressWarnings("unused")
    public boolean tryLock(NameSpace nameSpace, String key, String encryption, long expire) {
        return tryLockFallbackable(nameSpace, key, encryption, expire, false);
    }

    public boolean tryLockFallbackable(NameSpace nameSpace, String key, String encryption, long expire, boolean fallbackable) {
        key = nameSpace.getName() + "_" + key;
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

    static final String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @SuppressWarnings("unused")
    public boolean unlock(NameSpace nameSpace, String key, String encryption) {
        return unlockFallbackable(nameSpace, key, encryption, false);
    }

    public boolean unlockFallbackable(NameSpace nameSpace, String key, String encryption, boolean fallbackable) {
        key = nameSpace.getName() + "_" + key;
        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.eval(script, Collect.asList(key), Collect.asList(encryption));
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

    public boolean set(NameSpace nameSpace, String key, String value, long expire) {
        return setFallbackable(nameSpace, key, value, expire, false);
    }

    public boolean setFallbackable(NameSpace nameSpace, String key, String value, long expire, boolean fallbackable) {
        key = nameSpace.getName() + "_" + key;
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
    public String get(NameSpace nameSpace, String key) {
        return getFallbackable(nameSpace, key, false);
    }

    @Nullable
    public String getFallbackable(NameSpace nameSpace, String key, boolean fallbackable) {
        key = nameSpace.getName() + "_" + key;
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
    public boolean delete(NameSpace nameSpace, String key) {
        return deleteFallbackable(nameSpace, key, false);
    }

    public boolean deleteFallbackable(NameSpace nameSpace, String key, boolean fallbackable) {
        key = nameSpace.getName() + "_" + key;
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
