package com.stellariver.milky.common.tool.wire;

import com.stellariver.milky.common.base.BeanUtil;
import com.stellariver.milky.common.base.ErrorEnumsBase;
import com.stellariver.milky.common.base.SysEx;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StaticWireSupport {

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static void wire(Reflections reflections) {
        boolean contains = reflections.getStore().keySet().contains(FieldAnnotationsScanner.class.getName());
        if (!contains) {
            return;
        }
        List<Field> fields = reflections.getFieldsAnnotatedWith(StaticWire.class).stream()
                .peek(field -> SysEx.falseThrow(Modifier.isStatic(field.getModifiers()),
                                ErrorEnumsBase.CONFIG_ERROR.message("StaticWire should be annotated a static field!")))
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toList());
        for (Field field : fields) {
            Object o = field.get(null);
            SysEx.trueThrow(o != null, ErrorEnumsBase.CONFIG_ERROR.message(
                    field.toGenericString()  + " is not null, static wire bean should null before wired!"));
            StaticWire annotation = field.getAnnotation(StaticWire.class);
            Optional<Object> beanOptional;
            if (StringUtils.isNotBlank(annotation.name())) {
                beanOptional = BeanUtil.getBeanOptional(annotation.name());
            } else {
                beanOptional = (Optional<Object>) BeanUtil.getBeanOptional(field.getType());
            }
            if (beanOptional.isPresent()) {
                field.set(null, beanOptional.get());
            } else if (annotation.required()) {
                throw new SysEx(ErrorEnumsBase.CONFIG_ERROR.message("not found bean " + field.toGenericString()));
            }
        }
    }

    @SneakyThrows
    public static void unWire(Reflections reflections) {
        boolean contains = reflections.getStore().keySet().contains(FieldAnnotationsScanner.class.getName());
        if (!contains) {
            return;
        }
        reflections.getFieldsAnnotatedWith(StaticWire.class).stream().peek(field -> field.setAccessible(true))
                .forEach(field -> {
                    try {
                        field.set(null, null);
                    } catch (IllegalAccessException ignore) {}
                });

    }



}
