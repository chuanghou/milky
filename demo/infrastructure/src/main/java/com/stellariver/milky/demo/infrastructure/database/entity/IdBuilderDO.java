package com.stellariver.milky.demo.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stellariver.milky.infrastructure.base.database.AbstractMpDO;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author houchuang
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("id_builder")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdBuilderDO extends AbstractMpDO {

    @TableId(type = IdType.AUTO)
    Long id;

    String nameSpace;

    Long start;

    Long uniqueId;

    Long ceiling;

    Integer step;

    Long alarmThreshold;

    String duty;

}
