package com.stellariver.milky.infrastructure.base.database;

import com.baomidou.mybatisplus.annotation.*;
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

    @TableId(type = IdType.INPUT)
    Long id;

    @Version
    @TableField(fill = FieldFill.INSERT)
    Integer version;

    @TableLogic(delval = "id")
    @TableField(fill = FieldFill.INSERT)
    Long deleted;

    @TableField(fill = FieldFill.INSERT)
    Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    Date gmtModified;

}
