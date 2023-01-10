package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.base.BaseDataObject;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataObjectInfo {

    Class<? extends BaseDataObject<?>> clazz;

    Object primaryId;

}
