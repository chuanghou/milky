package com.stellariver.milky.demo.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.stellariver.milky.infrastructure.base.database.FullTextHandler;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "demo_meta_unit", autoResultMap = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DemoMetaUnit {

  @TableId(type = IdType.INPUT)
  Integer metaUnitId;
  String name;
  String province;
  @TableField(typeHandler = JacksonTypeHandler.class)
  UnitType unitType;
  Integer sourceId;
  String capacity;
  @TableField(typeHandler = FullTextHandler.class)
  List<String> generatorType;

}
