package com.stellariver.milky.demo.infrastructure.nacos.stable;

import com.stellariver.milky.common.tool.common.*;
import com.stellariver.milky.common.tool.stable.AbstractStableSupport;
import com.stellariver.milky.common.tool.stable.StableConfig;
import com.stellariver.milky.common.tool.util.Json;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author houchuang
 */
@Component
public class StableSupport extends AbstractStableSupport implements FakeConfigCenterListener, InitializingBean {

    @Override
    public void receiveMessage(String message) {
        StableConfig stableConfig = Json.parse(message, StableConfig.class);
        update(stableConfig);
    }

    @Override
    public void afterPropertiesSet() {
        Runner.setAbstractStableSupport(this);
    }


}
