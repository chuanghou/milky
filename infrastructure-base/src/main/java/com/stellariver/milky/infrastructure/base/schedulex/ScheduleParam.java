package com.stellariver.milky.infrastructure.base.schedulex;


import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Map;

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

