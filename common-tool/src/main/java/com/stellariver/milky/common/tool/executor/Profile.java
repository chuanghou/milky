package com.stellariver.milky.common.tool.executor;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile {

    String identify;
    Thread thread;
    LocalTime submitTime;
    LocalTime startTime;
    LocalTime endTime;
    Boolean history;

}
