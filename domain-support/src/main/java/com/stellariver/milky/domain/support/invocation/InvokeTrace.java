package com.stellariver.milky.domain.support.invocation;

import com.stellariver.milky.domain.support.base.Message;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvokeTrace {

    private Long invocationId;

    private Long lastTriggerId;

    static public InvokeTrace build(Message message) {
        return new InvokeTrace(message.getInvokeTrace().getInvocationId(), message.getId());
    }
}
