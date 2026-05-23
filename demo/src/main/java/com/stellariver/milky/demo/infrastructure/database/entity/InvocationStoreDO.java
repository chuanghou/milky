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
@TableName("invocation_store")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvocationStoreDO extends AbstractMpDO implements BaseDataObject<Long> {

    String operatorId;

    String operatorName;

    String operatorSource;

    Boolean success;

    @Override
    public Long getPrimaryId() {
        return getId();
    }
}
