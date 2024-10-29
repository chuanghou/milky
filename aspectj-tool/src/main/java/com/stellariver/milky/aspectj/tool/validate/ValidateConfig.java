package com.stellariver.milky.aspectj.tool.validate;

import com.stellariver.milky.common.base.ExceptionType;

import jakarta.validation.groups.Default;

public class ValidateConfig {

    private ExceptionType type = ExceptionType.BIZ;
    private Class<?>[] groups = { Default.class };
    boolean failFast = true;

    public ExceptionType getType() {
        return type;
    }

    public void setType(ExceptionType type) {
        this.type = type;
    }

    public Class<?>[] getGroups() {
        return groups;
    }

    public void setGroups(Class<?>[] groups) {
        this.groups = groups;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    static private final ValidateConfig DEFAULT_INSTANCE = new ValidateConfig();

    static public ValidateConfig defaultConfig() {
        return DEFAULT_INSTANCE;
    }

}
