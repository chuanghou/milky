package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * @author houchuang
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractMpDO {

    @Version
    @TableField(fill = FieldFill.INSERT)
    Integer version;

    /**
     * 逻辑删：未删除为 0；删除后 MP 生成 {@code SET deleted = id}（{@code delval} 须与主键<b>数据库列名</b>一致）。
     * 字段须为非 String，否则 delval 会被当作字符串常量而非列引用。
     */
    @TableLogic(value = "0", delval = "id")
    @TableField(fill = FieldFill.INSERT)
    Long deleted;

    @TableField(fill = FieldFill.INSERT)
    Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    Date gmtModified;

}
