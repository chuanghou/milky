package com.echobaba.milky.common.tool.log;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * @author houchuang.hc
 * @date 2021/11/16
 */
@Slf4j
public class MDCTool {

    static public String putAndGet(MDCTag mdcTag, LogTagValue logTagValue) {
        String originalValue = MDC.get(mdcTag.name());
        MDC.put(mdcTag.name(), logTagValue.getValue());
        return originalValue;
    }

    static public String putAndGet(MDCTag mdcTag, String value) {
        String originalValue = MDC.get(mdcTag.name());
        MDC.put(mdcTag.name(), value);
        return originalValue;
    }

    static public void removeAndRestore(MDCTag mdcTag, String value) {
        MDC.remove(mdcTag.name());
        MDC.put(mdcTag.name(), value);
    }

}


