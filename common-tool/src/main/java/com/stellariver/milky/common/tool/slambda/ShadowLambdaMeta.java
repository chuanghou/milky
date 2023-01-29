package com.stellariver.milky.common.tool.slambda;


import lombok.SneakyThrows;

public class ShadowLambdaMeta implements LambdaMeta {

    private final SerializedLambda lambda;

    public ShadowLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    @SneakyThrows
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(";")).replace("/", ".");
        return Class.forName(instantiatedType, true, lambda.getCapturingClass().getClassLoader());
    }

}
