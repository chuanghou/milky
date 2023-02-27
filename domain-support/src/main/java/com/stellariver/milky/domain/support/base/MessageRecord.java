package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author houchuang
 */
@Data
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageRecord {

    String beanName;

    Message message;

    List<Message> messages;

    Map<Class<? extends Typed<?>>, Object> dependencies;

}
