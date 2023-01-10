package com.stellariver.milky.common.tool.stable;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StableConfig {

    List<CbConfig> cbConfigs;

    List<RlConfig> rlConfigs;

}
