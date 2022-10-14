package com.stellariver.milky.demo.infrastructure.database.enitity;

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
@TableName("inventory")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryDO implements BaseDataObject<Long> {

    @TableId(type = IdType.INPUT)
    Long itemId;

    Long amount;

    String storeCode;

    @Override
    public Long getPrimaryId() {
        return itemId;
    }
}
