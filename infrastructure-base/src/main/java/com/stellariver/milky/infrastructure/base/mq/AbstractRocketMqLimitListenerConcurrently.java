package com.stellariver.milky.infrastructure.base.mq;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.SystemClock;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.infrastructure.base.ErrorEnum;
import lombok.CustomLog;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

@CustomLog
public abstract class AbstractRocketMqLimitListenerConcurrently
        extends BaseRocketMqListener implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(
            List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        if (Collect.isEmpty(msgs)) {
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        BizException.trueThrow(msgs.size() > 1, ErrorEnum.CONFIG_ERROR.message("batch size not 1"));
        MessageExt messageExt = msgs.get(0);
        Throwable throwable = null;
        long now = SystemClock.now();
        try {
            doBusinessWithFlowControl(messageExt);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Throwable e) {
            throwable = e;
            boolean retryable = messageExt.getReconsumeTimes() < 10;
            if (retryable) {
                throwable = new BizException(ErrorEnum.MESSAGE_RETRY, e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            } else {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        } finally {
            log.arg0(mqBodyLogger(messageExt)).arg1(messageExt.getKeys())
                    .arg2(messageExt.getTags()).log(messageExt.getTopic(), throwable);
        }
    }

}
