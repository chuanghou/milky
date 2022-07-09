package com.stellariver.milky.infrastructure.base.mq;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.SystemClock;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.infrastructure.base.ErrorEnum;
import lombok.CustomLog;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

@CustomLog
public abstract class AbstractRocketMqLimitListenerOrderly
        extends BaseRocketMqListener implements MessageListenerOrderly {

    @Override
    public ConsumeOrderlyStatus consumeMessage(
            List<MessageExt> msgs, ConsumeOrderlyContext consumeConcurrentlyContext) {
        if (Collect.isEmpty(msgs)) {
            return ConsumeOrderlyStatus.SUCCESS;
        }
        BizException.trueThrow(msgs.size() > 1, ErrorEnum.CONFIG_ERROR.message("batch size not 1"));
        MessageExt messageExt = msgs.get(0);
        Throwable throwable = null;
        long now = SystemClock.now();
        try {
            doBusinessWithFlowControl(messageExt);
            return ConsumeOrderlyStatus.SUCCESS;
        } catch (Throwable e) {
            throwable = e;
            boolean retryable;
            try {
                retryable = retryWhenRetryable(messageExt);
            } catch (Throwable t) {
                log.arg0(messageExt).error("RETRY_EXCEPTION", t);
                retryable = false;
            }
            if (retryable) {
                throwable = new BizException(ErrorEnum.MESSAGE_RETRY, e);
            }
            return ConsumeOrderlyStatus.SUCCESS;
        } finally {
            log.arg0(mqBodyLogger(messageExt)).arg1(messageExt.getKeys())
                    .arg2(messageExt.getTags()).log(messageExt.getTopic(), throwable);
        }
    }

    abstract public boolean retryWhenRetryable(MessageExt msg);

}
