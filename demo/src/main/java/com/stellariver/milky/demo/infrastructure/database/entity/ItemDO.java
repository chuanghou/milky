package com.stellariver.milky.demo.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stellariver.milky.domain.support.base.BaseDataObject;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("item")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDO implements BaseDataObject<Long> {

    @TableId(type = IdType.INPUT)
    Long itemId;

    String title;

    Long userId;

    String userName;

    Long amount;

    String storeCode;

    @Override
    public Long getPrimaryId() {
        return itemId;
    }
}
