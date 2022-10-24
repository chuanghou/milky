package com.stellariver.milky.infrastructure.base.mq;

import com.stellariver.milky.common.tool.common.BizException;
import com.stellariver.milky.common.tool.common.LogChoice;
import com.stellariver.milky.common.tool.common.SystemClock;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.infrastructure.base.ErrorEnums;
import lombok.CustomLog;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Objects;

@CustomLog
public abstract class AbstractRocketMQLimitMessageListenerOrderly extends BaseRocketMQLimitMessageListener implements MessageListenerOrderly {

    @Override
    public ConsumeOrderlyStatus consumeMessage(final List<MessageExt> msgs, final ConsumeOrderlyContext context){
        if (Collect.isEmpty(msgs)) {
            return ConsumeOrderlyStatus.SUCCESS;
        }
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
                throwable = new BizException(ErrorEnums.MESSAGE_RETRY, throwable);
            }
            return ConsumeOrderlyStatus.SUCCESS;
        } finally {
            finalWork();
            log.arg0(mqBodyLogger(messageExt)).arg1(messageExt.getKeys()).arg2(messageExt.getTags())
                    .cost(SystemClock.now() - now);
            if (Objects.equals(logChoice(), LogChoice.ALWAYS)) {
                log.log(messageExt.getTopic(), throwable);
            } else if(Objects.equals(logChoice(), LogChoice.EXCEPTION)) {
                log.logWhenException(messageExt.getTopic(), throwable);
            } else {
                log.error("UNREACHED_PART", throwable);
            }
        }
    }

    abstract public boolean retryWhenRetryable(MessageExt msg);

}
