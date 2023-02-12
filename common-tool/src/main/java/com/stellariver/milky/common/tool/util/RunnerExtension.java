package com.stellariver.milky.common.tool.util;

import com.stellariver.milky.common.tool.common.UK;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author houchuang
 */
public interface RunnerExtension {

    void watch(List<Object> args, @Nullable Object result, @Nullable UK lambdaId, @Nullable Throwable throwable);

}
