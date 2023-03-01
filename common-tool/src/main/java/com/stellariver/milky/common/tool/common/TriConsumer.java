
package com.stellariver.milky.common.tool.common;

@FunctionalInterface
public interface TriConsumer<T, U, R> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param r the third input argument
     */
    void accept(T t, U u, R r);

}
