package com.stellariver.milky.common.tool.common;

import java.util.function.Supplier;

public class Op<T> {

    private T t;

    private Supplier<T> supplier;

    private Op(Supplier<T> supplier) {
        if (supplier == null) {
            throw new NullPointerException();
        }
        this.supplier = supplier;
    }

    public T get() {
        return t;
    }

    public Op<T> orElse(Supplier<T> supplier) {
        this.t = supplier.get();
        if (this.t == null) {
            this.supplier = supplier;
        }
        return this;
    }

    public T orElse(T defaultValue) {
        if (this.t == null) {
            return defaultValue;
        } else {
            return t;
        }
    }


    static public <T> Op<T> op(Supplier<T> supplier) {
        return new Op<>(supplier);
    }

    public static void main(String[] args) {
        String dssd = Op.op(() -> "sds").orElse(() -> "sddds").orElse("dssd");
    }
}
