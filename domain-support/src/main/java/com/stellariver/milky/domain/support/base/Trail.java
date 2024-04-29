package com.stellariver.milky.domain.support.base;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author houchuang
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Trail {

    String beanName;

    Message message;

    Object result;

    List<Trail> subTrails;

}
