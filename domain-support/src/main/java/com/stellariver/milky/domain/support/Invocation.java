package com.stellariver.milky.domain.support;

import com.stellariver.milky.common.tool.util.BeanUtils;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class Invocation {
    private Map<String, Object> parameters;

    private Long invocationId;

    private Invocation(Map<String, Object> parameters) {
        this.parameters = parameters;
        this.invocationId = BeanUtils.getBean(IdBuilder.class).build();
    }

    static public Invocation build(Map<String, Object> parameters) {
        parameters = Optional.ofNullable(parameters).orElseGet(HashMap::new);
        return new Invocation(parameters);
    }

    static public Invocation build() {
        return new Invocation(new HashMap<>());
    }

}
