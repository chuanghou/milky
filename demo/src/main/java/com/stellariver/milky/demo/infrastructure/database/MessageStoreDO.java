package com.stellariver.milky.demo.infrastructure.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@TableName("message_store")
public class MessageStoreDO {

    @TableId(type = IdType.INPUT)
    Long id;

    String aggregateId;

    Long invocationId;

    Long triggerId;

    String className;

}