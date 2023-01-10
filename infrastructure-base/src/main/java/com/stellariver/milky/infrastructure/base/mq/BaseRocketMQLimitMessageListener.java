package com.stellariver.milky.infrastructure.base.mq;

import lombok.CustomLog;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author houchuang
 */
@CustomLog
public abstract class BaseRocketMQLimitMessageListener extends BaseLimitMessageListener {

    protected void doBusinessWithFlowControl(MessageExt msg) {
        flowControl();
        doBusiness(msg);
    }

    protected abstract void doBusiness(MessageExt msg);

    protected String mqBodyLogger(MessageExt msg) {
        return "";
    }

}
