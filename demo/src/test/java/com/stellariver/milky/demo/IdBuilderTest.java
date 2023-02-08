package com.stellariver.milky.demo;

import com.stellariver.milky.domain.support.dependency.IdBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IdBuilderTest {

    @Autowired
    IdBuilder idBuilder;

    @Test
    public void idBuilderTest() {
        for (int i = 0; i < 1000; i++) {
            System.out.println(idBuilder.get());
        }
    }

}
