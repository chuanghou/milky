package com.stellariver.milky.domain.support.base;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author houchuang
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MilkyConfiguration {

    String[] scanPackages;

}