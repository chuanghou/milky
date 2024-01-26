package com.stellariver.milky.common.tool.executor;

import java.util.concurrent.Callable;

public interface IdentifiedCallable<V> extends Callable<V> {

    String getIdentify();

}
