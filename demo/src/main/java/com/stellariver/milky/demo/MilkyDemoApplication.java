package com.stellariver.milky.demo;

import com.stellariver.milky.starter.EnableMilky;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

@EnableMilky("com.stellariver.milky.demo")
@SpringBootApplication
public class MilkyDemoApplication {

    public static void main(String[] args) {

        SpringApplication.run(MilkyDemoApplication.class, args);

        ClassPathResource classPathResource = new ClassPathResource("");

        DefaultListableBeanFactory defaultListableBeanFactory = new DefaultListableBeanFactory();

        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(defaultListableBeanFactory);

        int i = xmlBeanDefinitionReader.loadBeanDefinitions(classPathResource);



    }
}
