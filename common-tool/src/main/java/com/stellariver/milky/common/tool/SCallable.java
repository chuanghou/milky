package com.stellariver.milky.common.tool;

import java.io.Serializable;
import java.util.concurrent.Callable;

public interface SCallable<V> extends Callable<V>, Serializable {}
