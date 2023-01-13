package com.stellariver.milky.common.tool.stable;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StableConfig {

    @Builder.Default
    Map<String, CbConfig> cbConfigs = new HashMap<>();

    @Builder.Default
    Map<String, RlConfig> rlConfigs = new HashMap<>();

}
