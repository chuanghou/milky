package com.stellariver.milky.demo.infrastructure.database.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("inventory")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryDO extends AbstractMpDO implements BaseDataObject<Long> {

    Long amount;

    String storeCode;

    public Long getInventoryId() {
        return getId();
    }

    public void setInventoryId(Long inventoryId) {
        setId(inventoryId);
    }

    public static abstract class InventoryDOBuilder<C extends InventoryDO, B extends InventoryDOBuilder<C, B>>
            extends AbstractMpDOBuilder<C, B> {

        public B inventoryId(Long inventoryId) {
            return id(inventoryId);
        }

        /** @deprecated use {@link #inventoryId(Long)} */
        public B itemId(Long itemId) {
            return inventoryId(itemId);
        }
    }

    @Override
    public Long getPrimaryId() {
        return getInventoryId();
    }
}
