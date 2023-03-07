package com.stellariver.milky.infrastructure.base.mq;


import com.stellariver.milky.common.tool.exception.BizEx;
import com.stellariver.milky.common.tool.common.Clock;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.infrastructure.base.ErrorEnums;
import lombok.CustomLog;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author houchuang
 */
@CustomLog
public abstract class AbstractRocketMQLimitMessageListenerConcurrently extends BaseRocketMQLimitMessageListener implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        if (Collect.isEmpty(msgs)) {
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        MessageExt messageExt = msgs.get(0);
        Throwable throwable = null;
        long now = Clock.currentTimeMillis();
        try {
            doBusinessWithFlowControl(messageExt);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Throwable e) {
            throwable = e;
            boolean retryable = messageExt.getReconsumeTimes() < 10;
            if (retryable) {
                throwable = new BizEx(ErrorEnums.MESSAGE_RETRY, e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            } else {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        } finally {
            finalWork();
            log.arg0(mqBodyLogger(messageExt)).arg1(messageExt.getKeys()).arg2(messageExt.getTags())
                    .cost(Clock.currentTimeMillis() - now);
            if (alwaysLog()) {
                log.log(this.getClass().getSimpleName(), throwable);
            } else {
                log.logWhenException(this.getClass().getSimpleName(), throwable);
            }
        }
    }

}
