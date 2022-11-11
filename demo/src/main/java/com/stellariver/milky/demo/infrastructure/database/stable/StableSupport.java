package com.stellariver.milky.demo.infrastructure.database.stable;

import com.stellariver.milky.common.tool.common.*;
import com.stellariver.milky.common.tool.exception.SysException;
import com.stellariver.milky.common.tool.stable.AbstractStableSupport;
import com.stellariver.milky.common.tool.stable.CbConfig;
import com.stellariver.milky.common.tool.stable.RlConfig;
import com.stellariver.milky.common.tool.stable.StableConfig;
import com.stellariver.milky.common.tool.util.Collect;
import com.stellariver.milky.common.tool.util.Json;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StableSupport extends AbstractStableSupport implements FakeConfigCenterListener, InitializingBean {

    @Override
    public void receiveMessage(String message) {
        StableConfig stableConfig = Json.parse(message, StableConfig.class);
        SysException.nullThrow(stableConfig);
        Map<String, CbConfig> cbConfigs = Collect.toMap(stableConfig.getCbConfigs(), CbConfig::getKey);
        setCbConfigs(cbConfigs);
        clearCircuitBreakers();
        Map<String, RlConfig> rlConfigs = Collect.toMap(stableConfig.getRlConfigs(), RlConfig::getKey);
        setRlConfigs(rlConfigs);
        clearRateLimiters();
    }

    @Override
    public void afterPropertiesSet() {
        Runner.setAbstractStableSupport(this);
    }


}
