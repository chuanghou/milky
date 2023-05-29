package com.stellariver.milky.spring.partner.wire;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaticWireScanPackages {

    String[] scanPackages;

}
