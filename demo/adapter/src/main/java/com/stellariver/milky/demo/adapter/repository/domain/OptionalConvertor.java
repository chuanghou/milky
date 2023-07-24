package com.stellariver.milky.demo.adapter.repository.domain;

import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface OptionalConvertor {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    default String toString(Optional<String> optional) {
        return optional.orElse(null);
    }

    @BeanMapping(builder = @Builder(disableBuilder = true))
    default Optional<String> toOptional(String t) {
        return Optional.ofNullable(t);
    }


    @BeanMapping(builder = @Builder(disableBuilder = true))
    default Integer toInteger(Optional<Integer> optional) {
        return optional.orElse(null);
    }

    @BeanMapping(builder = @Builder(disableBuilder = true))
    default Optional<Integer> toOptional(Integer t) {
        return Optional.ofNullable(t);
    }


    @BeanMapping(builder = @Builder(disableBuilder = true))
    default Long toLong(Optional<Long> optional) {
        return optional.orElse(null);
    }

    @BeanMapping(builder = @Builder(disableBuilder = true))
    default Optional<Long> toOptional(Long t) {
        return Optional.ofNullable(t);
    }


    @BeanMapping(builder = @Builder(disableBuilder = true))
    default Boolean toBoolean(Optional<Boolean> optional) {
        return optional.orElse(null);
    }

    @BeanMapping(builder = @Builder(disableBuilder = true))
    default Optional<Boolean> toOptional(Boolean t) {
        return Optional.ofNullable(t);
    }

}
