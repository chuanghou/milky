package com.stellariver.milky.demo.infrastructure.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("item")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDO {

    @TableId(type = IdType.INPUT)
    Long itemId;

    String title;

    Long sellerId;

    String userName;

}
