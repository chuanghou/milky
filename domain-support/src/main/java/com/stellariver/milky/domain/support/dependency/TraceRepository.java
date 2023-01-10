package com.stellariver.milky.domain.support.dependency;

import com.stellariver.milky.domain.support.context.Context;

/**
 * @author houchuang
 */
public interface TraceRepository {

    void record(Context context, boolean success);

}
