package com.stellariver.milky.domain.support.invocation;

import com.stellariver.milky.domain.support.base.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author houchuang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvokeTrace {

    private Long invocationId;

    private Long triggerId;

    static public InvokeTrace build(Message message) {
        Long messageId = message.getId();
        Long invocationId = message.getInvokeTrace().getInvocationId();
        return new InvokeTrace(invocationId, messageId);
    }
}
