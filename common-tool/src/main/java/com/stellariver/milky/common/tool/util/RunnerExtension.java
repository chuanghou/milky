package com.stellariver.milky.common.tool.util;

import com.stellariver.milky.common.tool.common.UK;

import javax.annotation.Nullable;
import java.lang.invoke.SerializedLambda;

/**
 * @author houchuang
 */
public interface RunnerExtension {

    void watch(SerializedLambda args, @Nullable Object result, @Nullable UK lambdaId, @Nullable Throwable throwable);

}
