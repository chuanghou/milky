package com.stellariver.milky.demo;


import com.stellariver.milky.demo.infrastructure.database.entity.DemoMetaUnit;
import com.stellariver.milky.demo.infrastructure.database.entity.UnitType;
import com.stellariver.milky.demo.infrastructure.database.mapper.DemoMetaUnitMapper;
import com.stellariver.milky.infrastructure.base.database.MilkyLogFilter;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;

@CustomLog
@SpringBootTest
@DirtiesContext
public class LogTest {

    @Autowired
    DemoMetaUnitMapper demoMetaUnitMapper;


    @Test
    public void testFullTextHandler() {
        DemoMetaUnit build = DemoMetaUnit.builder()
                .name("test")
                .unitType(new UnitType("work"))
                .generatorType(Arrays.asList("111", "2211"))
                .province("test")
                .metaUnitId(1)
                .capacity("1")
                .sourceId(1)
                .build();

        demoMetaUnitMapper.insert(build);

        DemoMetaUnit demoMetaUnit = demoMetaUnitMapper.selectById(1);
        Assertions.assertEquals(build, demoMetaUnit);
        demoMetaUnit = MilkyLogFilter.byPass(() -> demoMetaUnitMapper.selectById(1));
        log.info("ignore success");
        demoMetaUnit = demoMetaUnitMapper.selectById(1);
        demoMetaUnitMapper.deleteById(1);
    }
}
