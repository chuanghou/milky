package com.stellariver.milky.domain.support.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventRecord extends MessageRecord{

}
