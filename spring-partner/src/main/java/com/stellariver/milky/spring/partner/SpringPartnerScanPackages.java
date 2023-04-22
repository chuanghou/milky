package com.stellariver.milky.spring.partner;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpringPartnerScanPackages {

    String[] scanPackages;

}
