package com.stellariver.milky.common.tool.stable;

import com.stellariver.milky.common.base.BeanUtil;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;
import java.util.stream.Collectors;

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

    @Builder.Default
    Map<String, String> keyRuleIdMap = new HashMap<>();

    volatile private List<StableRuleIdRouter> routers;

    public String matchRuleId(@NonNull String key) {

        if (routers == null) {
            synchronized (this) {
                if (routers == null) {
                    routers = BeanUtil.getBeansOfType(StableRuleIdRouter.class);
                }
            }
        }

        List<String> ruleIds = routers.stream().map(router -> router.route(key)).filter(Objects::nonNull).collect(Collectors.toList());

        if (ruleIds.size() > 1) {
            throw new IllegalArgumentException(key + " could be math to at least two ruleIds" + ruleIds);
        }

        if (ruleIds.size() != 0) {
            return ruleIds.get(0);
        }

        return Optional.ofNullable(keyRuleIdMap.get(key)).orElse(key);

    }

}
