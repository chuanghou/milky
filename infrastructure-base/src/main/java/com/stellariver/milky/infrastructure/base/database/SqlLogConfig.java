package com.stellariver.milky.infrastructure.base.database;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SqlLogConfig {

    /**
     * the alarm sql cost threshold by milliseconds
     * any sql cost supersede this number will have an error log
     */
    Integer alarmSqlCostThreshold = 3000;


    /**
     * the count of sql records supersede this record, there will be
     * an error log
     */
    Integer alarmSqlCountThreshold = 1000;



    /**
     * enable sql sql
     */
    Boolean enableSelectSqlGlobal = false;

}
