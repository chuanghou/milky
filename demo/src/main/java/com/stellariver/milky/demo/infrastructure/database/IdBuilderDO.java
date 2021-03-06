package com.stellariver.milky.demo.infrastructure.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.awt.*;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@TableName("id_builder")
public class IdBuilderDO {

    @TableId(type = IdType.AUTO)
    Long id;

    String nameSpace;

}
