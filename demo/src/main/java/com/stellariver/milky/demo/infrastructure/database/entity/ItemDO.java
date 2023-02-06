package com.stellariver.milky.demo.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.stellariver.milky.demo.common.enums.ChannelEnum;
import com.stellariver.milky.domain.support.base.BaseDataObject;
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
@TableName("item")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDO extends AbstractMpDO implements BaseDataObject<Long> {

    @TableId(type = IdType.INPUT)
    Long itemId;

    String title;

    Long userId;

    String userName;

    Long amount;

    String storeCode;

    ChannelEnum channelEnum;

    Long ratio;

    @Override
    public Long getPrimaryId() {
        return itemId;
    }

}
