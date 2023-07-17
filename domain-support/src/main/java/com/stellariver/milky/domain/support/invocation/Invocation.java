package com.stellariver.milky.domain.support.invocation;

import com.stellariver.milky.common.base.BeanUtil;
import com.stellariver.milky.domain.support.dependency.UniqueIdGetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author houchuang
 */
@Data
public class Invocation {
    private Map<String, Object> parameters;

    private Long invocationId;

    private Invocation(Map<String, Object> parameters) {
        this.parameters = parameters;
        this.invocationId = BeanUtil.getBean(UniqueIdGetter.class).get();
    }

    static public Invocation build(Map<String, Object> parameters) {
        parameters = Optional.ofNullable(parameters).orElseGet(HashMap::new);
        return new Invocation(parameters);
    }

    static public Invocation build() {
        return new Invocation(new HashMap<>(16));
    }

}
