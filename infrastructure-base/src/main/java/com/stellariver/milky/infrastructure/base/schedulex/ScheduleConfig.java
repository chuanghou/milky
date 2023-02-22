package com.stellariver.milky.infrastructure.base.schedulex;

import com.stellariver.milky.common.tool.common.Clock;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleConfig {

    String simpleClassName;

    Boolean enable;

    Long startIndex;

    Long total;

    Long pageSize;

    @Builder.Default
    String ds = String.valueOf(Clock.beforeNow(1));

    Map<String, String> metadata;

}
