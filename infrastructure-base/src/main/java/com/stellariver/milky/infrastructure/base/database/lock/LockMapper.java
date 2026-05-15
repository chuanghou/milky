package com.stellariver.milky.infrastructure.base.database.lock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.UUID;

/**
 * 分布式锁Mapper
 * 提供基于数据库的分布式锁能力，支持超时设置和阻塞获取
 */
public interface LockMapper extends BaseMapper<LockDO> {

    /**
     * 生成UUID（不带横杠）
     *
     * @return UUID字符串
     */
    static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取锁持有者标识（UUID）
     *
     * @return 唯一标识
     */
    static String generateOwnerId() {
        return generateUuid();
    }

    /**
     * 尝试获取锁（非阻塞）
     * 使用INSERT ... ON DUPLICATE KEY UPDATE实现乐观锁
     *
     * @param key        锁键名
     * @param owner      锁持有者标识
     * @param expireTime 过期时间戳（毫秒）
     * @return 成功返回1，失败返回0
     */
    @Insert("INSERT INTO milky_lock_do (`key`, owner, expire_time, gmt_create, gmt_modified) " +
            "VALUES (#{key}, #{owner}, #{expireTime}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "owner = IF(expire_time < UNIX_TIMESTAMP() * 1000, #{owner}, owner), " +
            "expire_time = IF(expire_time < UNIX_TIMESTAMP() * 1000, #{expireTime}, expire_time), " +
            "gmt_modified = NOW()")
    int tryLock(@Param("key") String key,
            @Param("owner") String owner,
            @Param("expireTime") Long expireTime);

    /**
     * 尝试获取锁（非阻塞，便捷方法）
     *
     * @param key    锁键名
     * @param expire 锁过期时间
     * @return 锁持有者标识，如果获取失败返回null
     */
    @Nullable
    default String tryLock(String key, Duration expire) {
        String ownerId = generateOwnerId();
        long expireTime = System.currentTimeMillis() + expire.toMillis();
        int result = tryLock(key, ownerId, expireTime);
        return result == 1 ? ownerId : null;
    }

    /**
     * 获取锁信息
     *
     * @param key 锁键名
     * @return 锁实体，如果不存在返回null
     */
    @Nullable
    @Select("SELECT * FROM milky_lock_do WHERE `key` = #{key}")
    LockDO getLock(@Param("key") String key);

    /**
     * 释放锁（只能释放自己持有的锁）
     *
     * @param key   锁键名
     * @param owner 锁持有者标识
     * @return 成功返回1，失败返回0
     */
    @Update("UPDATE milky_lock_do SET expire_time = 0, gmt_modified = NOW() " +
            "WHERE `key` = #{key} AND owner = #{owner}")
    int unlock(@Param("key") String key, @Param("owner") String owner);

    /**
     * 强制释放锁（不管持有者）
     *
     * @param key 锁键名
     * @return 成功返回1，失败返回0
     */
    @Update("UPDATE milky_lock_do SET expire_time = 0, gmt_modified = NOW() " +
            "WHERE `key` = #{key}")
    int forceUnlock(@Param("key") String key);

    /**
     * 续期锁
     *
     * @param key        锁键名
     * @param owner      锁持有者标识
     * @param expireTime 新的过期时间戳（毫秒）
     * @return 成功返回1，失败返回0
     */
    @Update("UPDATE milky_lock_do SET expire_time = #{expireTime}, gmt_modified = NOW() " +
            "WHERE `key` = #{key} AND owner = #{owner}")
    int renewLock(@Param("key") String key,
            @Param("owner") String owner,
            @Param("expireTime") Long expireTime);

    /**
     * 续期锁（便捷方法）
     *
     * @param key    锁键名
     * @param owner  锁持有者标识
     * @param expire 新的过期时间
     * @return 是否续期成功
     */
    default boolean renewLock(String key, String owner, Duration expire) {
        long expireTime = System.currentTimeMillis() + expire.toMillis();
        return renewLock(key, owner, expireTime) == 1;
    }

    /**
     * 尝试获取锁（带超时阻塞）
     * 通过循环尝试获取锁，直到获取成功或超时
     *
     * @param key           锁键名
     * @param owner         锁持有者标识
     * @param expireTime    过期时间戳（毫秒）
     * @param waitTimeout   等待超时时间（毫秒）
     * @param retryInterval 重试间隔（毫秒）
     * @return 成功返回true，超时返回false
     */
    default boolean tryLockWithTimeout(String key, String owner,
            long expireTime, long waitTimeout, long retryInterval) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + waitTimeout;

        while (System.currentTimeMillis() < endTime) {
            int result = tryLock(key, owner, expireTime);
            if (result == 1) {
                return true;
            }

            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    /**
     * 尝试获取锁（带超时阻塞，使用Duration）
     *
     * @param key           锁键名
     * @param owner         锁持有者标识
     * @param expire        过期时间
     * @param waitTimeout   等待超时时间
     * @param retryInterval 重试间隔
     * @return 成功返回true，超时返回false
     */
    default boolean tryLockWithTimeout(String key, String owner,
            Duration expire, Duration waitTimeout, Duration retryInterval) {
        long expireTime = System.currentTimeMillis() + expire.toMillis();
        return tryLockWithTimeout(key, owner, expireTime,
                waitTimeout.toMillis(), retryInterval.toMillis());
    }

    /**
     * 尝试获取锁（带超时阻塞，使用Duration，默认重试间隔10ms）
     *
     * @param key         锁键名
     * @param owner       锁持有者标识
     * @param expire      过期时间
     * @param waitTimeout 等待超时时间
     * @return 成功返回true，超时返回false
     */
    default boolean tryLockWithTimeout(String key, String owner,
            Duration expire, Duration waitTimeout) {
        return tryLockWithTimeout(key, owner, expire, waitTimeout, Duration.ofMillis(10));
    }

    /**
     * 尝试获取锁（带超时阻塞，便捷方法）
     *
     * @param key    锁键名
     * @param expire 锁过期时间
     * @param wait   等待超时时间
     * @return 锁持有者标识，如果获取失败返回null
     */
    @Nullable
    default String tryLockWithTimeout(String key, Duration expire, Duration wait) {
        String ownerId = generateOwnerId();
        boolean success = tryLockWithTimeout(key, ownerId, expire, wait, Duration.ofMillis(10));
        return success ? ownerId : null;
    }

    /**
     * 判断锁是否存在且未过期
     * 释放锁时 expireTime 会被设置为 0，因此只需判断是否大于 0
     *
     * @param key 锁键名
     * @return 如果锁存在且未过期返回true，否则返回false
     */
    default boolean isLocked(String key) {
        LockDO lockDO = getLock(key);
        return lockDO != null && lockDO.getExpireTime() > 0;
    }
}
