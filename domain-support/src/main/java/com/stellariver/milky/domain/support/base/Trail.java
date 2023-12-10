package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.stellariver.milky.domain.support.dependency.Trace;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author houchuang
 */
@Data
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Trail {

    String beanName;

    List<Message> messages;

    List<Trace> traces;

    Object result;

}
