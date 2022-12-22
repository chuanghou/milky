package com.stellariver.milky.common.tool.util;

import java.util.List;
import java.util.Map;

public interface FailureExtendable {

    void watch(Map<String, Object> args, Throwable throwable);

}
