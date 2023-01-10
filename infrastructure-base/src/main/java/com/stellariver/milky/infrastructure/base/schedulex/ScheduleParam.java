package com.stellariver.milky.infrastructure.base.schedulex;

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
public class ScheduleParam {

    Long start;

    Long end;

    String ds;

    Map<String, String> metadata;

}
