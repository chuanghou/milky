package com.stellariver.milky.domain.support;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvokeTrace {

    private Long invocationId;

    private Long lastTriggerId;

}
