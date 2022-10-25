package com.stellariver.milky.infrastructure.base.schedulex;

import com.stellariver.milky.common.tool.util.SystemClock;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

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
    String ds = SystemClock.yesterdayDs();

    Map<String, String> metadata;

}
