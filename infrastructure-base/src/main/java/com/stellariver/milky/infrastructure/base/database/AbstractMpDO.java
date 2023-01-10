package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * @author houchuang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractMpDO {

    @Version
    @TableField(fill = FieldFill.INSERT)
    Integer version;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    Date gmtModified;

}
