package com.stellariver.milky.common.tool.slambda;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * @author houchuang
 */
public interface SCallable<V> extends Callable<V>, Serializable {}
