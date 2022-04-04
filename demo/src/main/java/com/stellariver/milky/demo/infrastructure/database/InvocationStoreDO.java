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
@TableName("invocation_store")
public class InvocationStoreDO {

    @TableId(type = IdType.INPUT)
    Long id;

    String operatorId;

    String operatorName;
}
