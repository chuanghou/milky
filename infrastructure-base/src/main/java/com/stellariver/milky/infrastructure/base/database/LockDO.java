package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * 分布式锁实体类
 * 基于数据库实现的分布式锁，支持超时设置和阻塞获取
 * 
 * <pre>{@code
 * CREATE TABLE `milky_lock_do` (
 *     `id` VARCHAR(32) NOT NULL COMMENT '主键ID（UUID，不带横杠）',
 *     `key` VARCHAR(128) NOT NULL COMMENT '锁键名',
 *     `owner` VARCHAR(32) NOT NULL COMMENT '锁持有者标识（UUID）',
 *     `expire_time` BIGINT NOT NULL COMMENT '锁过期时间戳（毫秒）',
 *     `gmt_create` DATETIME NOT NULL COMMENT '创建时间',
 *     `gmt_modified` DATETIME NOT NULL COMMENT '修改时间',
 *     PRIMARY KEY (`id`),
 *     UNIQUE KEY `uk_lock_key` (`key`) COMMENT '锁键名唯一索引，防止重复创建同一把锁',
 *     INDEX `idx_expire_time` (`expire_time`) COMMENT '过期时间索引，用于清理过期锁'
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分布式锁表';
 * }</pre>
 * 
 * <p>索引说明：</p>
 * <ul>
 *   <li><code>PRIMARY KEY (id)</code> - 主键索引，保证记录唯一性</li>
 *   <li><code>UNIQUE KEY uk_lock_key (key)</code> - 唯一索引，确保同一锁键名只能存在一条记录</li>
 *   <li><code>INDEX idx_expire_time (expire_time)</code> - 普通索引，加速过期锁查询和清理</li>
 * </ul>
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@TableName("milky_lock_do")
public class LockDO {

    /**
     * 主键ID，使用UUID
     */
    @TableId(value = "id", type = IdType.INPUT)
    String id;

    /**
     * 锁键名
     */
    @TableField("`key`")
    String key;

    /**
     * 锁持有者标识（UUID）
     */
    @TableField("owner")
    String owner;

    /**
     * 锁过期时间戳（毫秒）
     */
    @TableField("expire_time")
    Long expireTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    Date gmtCreate;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    Date gmtModified;

}
