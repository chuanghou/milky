package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.base.MessageRecord;
import com.stellariver.milky.domain.support.context.Context;

import java.util.List;

public interface TraceRepository {

    default void record(Context context) {
        record(context, true);
    }

    void record(Context context, boolean success);

}
