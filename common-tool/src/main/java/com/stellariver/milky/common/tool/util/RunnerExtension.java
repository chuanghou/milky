package com.stellariver.milky.common.tool.util;

import com.stellariver.milky.common.tool.common.UK;

import javax.annotation.Nullable;
import java.util.Map;

public interface RunnerExtension {

    void watch(Map<String, Object> args, @Nullable Object result, @Nullable UK lambdaId, @Nullable Throwable throwable);

}
