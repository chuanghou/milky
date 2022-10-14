package com.stellariver.milky.demo.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.stellariver.milky.infrastructure.base.database.MpAbstractDO;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("message_store")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageStoreDO extends MpAbstractDO {

    @TableId(type = IdType.INPUT)
    Long id;

    String aggregateId;

    Long invocationId;

    Long triggerId;

    String className;

}