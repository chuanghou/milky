package com.stellariver.milky.infrastructure.base.mq;

import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.infrastructure.base.ErrorEnums;
import lombok.CustomLog;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author houchuang
 */
@CustomLog
public abstract class AbstractRocketMQLimitMessageListenerOrderly extends BaseRocketMQLimitMessageListener implements MessageListenerOrderly {

    @Override
    public ConsumeOrderlyStatus consumeMessage(final List<MessageExt> msgs, final ConsumeOrderlyContext context){
        if (Collect.isEmpty(msgs)) {
            return ConsumeOrderlyStatus.SUCCESS;
        }
        MessageExt messageExt = msgs.get(0);
        Throwable throwable = null;
        long now = Clock.currentTimeMillis();
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
                throwable = new BizEx(ErrorEnums.MESSAGE_RETRY, throwable);
            }
            return ConsumeOrderlyStatus.SUCCESS;
        } finally {
            finalWork();
            log.arg0(mqBodyLogger(messageExt)).arg1(messageExt.getKeys()).arg2(messageExt.getTags())
                    .cost(Clock.currentTimeMillis() - now);
            if (alwaysLog()) {
                log.log(messageExt.getTopic(), throwable);
            } else {
                log.logWhenException(messageExt.getTopic(), throwable);
            }
        }
    }

    abstract public boolean retryWhenRetryable(MessageExt msg);

}
