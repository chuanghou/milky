package com.stellariver.milky.domain.support.depend;

import com.stellariver.milky.domain.support.base.Message;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
public class MessageQuery {

    List<String> aggregateId;

    Date start;

    Date end;

    List<Class<? extends Message>> classes;



}
