package com.stellariver.student.management.domain;

import com.stellariver.milky.domain.support.base.AggregateRoot;

public class User extends AggregateRoot {

    private Long userId;

    @Override
    public String getAggregateId() {
        return userId.toString();
    }
}
