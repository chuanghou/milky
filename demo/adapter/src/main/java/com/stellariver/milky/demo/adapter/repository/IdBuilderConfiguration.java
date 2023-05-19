package com.stellariver.milky.demo.adapter.repository;

import com.stellariver.milky.demo.infrastructure.database.mapper.IdBuilderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
public class IdBuilderConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public IdBuilder uniqueIdGetter(IdBuilderMapper idBuilderMapper) {
        Sequence sequence = Sequence.builder()
                .nameSpace("default")
                .start(1L)
                .step(100)
                .alarmRatio(0.9D)
                .ceiling(Long.MAX_VALUE)
                .duty(IdBuilder.Duty.NOT_WORK.name())
                .build();
        return new IdBuilder(sequence, idBuilderMapper);
    }

    @Component
    @RequiredArgsConstructor
    static class IdBuilderInitDB implements ApplicationRunner{

        final List<IdBuilder> idBuilders;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            idBuilders.forEach(IdBuilder::initDB);
        }

    }
}
