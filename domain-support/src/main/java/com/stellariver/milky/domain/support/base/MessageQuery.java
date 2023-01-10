package com.stellariver.milky.domain.support.base;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

/**
 * @author houchuang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
public class MessageQuery {

    List<String> aggregateId;

    List<Class<? extends Message>> classes;

    Date start;

    Date end;

}
