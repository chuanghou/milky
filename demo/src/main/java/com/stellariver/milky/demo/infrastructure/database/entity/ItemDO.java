package com.stellariver.milky.demo.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("item")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDO extends AbstractMpDO implements BaseDataObject<Long> {

    String title;

    Long userId;

    String userName;

    Long amount;

    String storeCode;

    String price;

    ChannelEnum channelEnum;

    Long ratio;

    public Long getItemId() {
        return getId();
    }

    public void setItemId(Long itemId) {
        setId(itemId);
    }

    public static abstract class ItemDOBuilder<C extends ItemDO, B extends ItemDOBuilder<C, B>>
            extends AbstractMpDOBuilder<C, B> {

        public B itemId(Long itemId) {
            return id(itemId);
        }
    }

    @Override
    public Long getPrimaryId() {
        return getItemId();
    }

}
