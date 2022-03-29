package com.stellariver.milky.domain.support.base;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MilkyConfiguration {

    boolean enableMq;

    String[] scanPackages;

}