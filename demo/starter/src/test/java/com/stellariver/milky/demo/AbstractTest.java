package com.stellariver.milky.demo;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@ContextConfiguration
@TestPropertySource("classpath:bootstrap.properties")
public abstract class AbstractTest {
}
