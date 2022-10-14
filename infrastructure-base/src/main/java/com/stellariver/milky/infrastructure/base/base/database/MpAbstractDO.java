package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class MpAbstractDO {

    @Version
    @Builder.Default
    @TableField(fill = FieldFill.INSERT)
    Long version = 0L;

    @TableLogic
    @Builder.Default
    @TableField(fill = FieldFill.INSERT)
    Long deleted = 0L;

    @Builder.Default
    @TableField(fill = FieldFill.INSERT)
    Date gmtCreate = new Date();

    @Builder.Default
    @TableField(fill = FieldFill.INSERT_UPDATE)
    Date gmtModified = new Date();

}
