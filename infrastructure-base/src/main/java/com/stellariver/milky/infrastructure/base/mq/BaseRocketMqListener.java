package com.stellariver.milky.infrastructure.base.mq;

import org.apache.rocketmq.common.message.MessageExt;

public abstract class BaseRocketMqListener extends BaseLimitMqListener {

    protected void doBusinessWithFlowControl(MessageExt msg) {
        flowControl();
        doBusiness(msg);
    }

    protected abstract void doBusiness(MessageExt msg);

    protected String mqBodyLogger(MessageExt msg) {
        return new String(msg.getBody());
    }

}
