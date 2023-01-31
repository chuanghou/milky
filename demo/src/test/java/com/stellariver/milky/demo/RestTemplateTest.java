//package com.stellariver.milky.demo;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.stellariver.milky.common.tool.common.Option;
//import com.stellariver.milky.common.tool.common.Runner;
//import com.stellariver.milky.common.tool.slambda.SCallable;
//import com.stellariver.milky.common.tool.util.Json;
//import com.stellariver.milky.demo.adapter.controller.Book;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.web.client.RestTemplate;
//
//@SpringBootTest
//@DirtiesContext
//class RestTemplateTest {
//
//    @Autowired
//    RestTemplate restTemplate;
//
//    @TestConfiguration
//    static class RestTemplateConfiguration {
//
//        @Bean
//        public RestTemplate restTemplate() {
//            return new RestTemplate();
//        }
//
//    }
//
//    @Test
//    public void restTemplateTest() {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
//        httpHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());
//
//        HttpEntity<Void> httpEntity = new HttpEntity<>(null, httpHeaders);
//
//        Option<JsonNode, Book> option = Option.<JsonNode, Book>builder()
//                .check(jsonNode -> Json.parseResult(jsonNode, Book.class).isSuccess())
//                .transfer(jsonNode -> Json.parseResult(jsonNode, Book.class).getData())
//                .build();
//
//        SCallable<JsonNode> sCallable = () -> restTemplate.postForObject("http://localhost/postForBook", httpEntity, JsonNode.class);
//        Book book = Runner.checkout(option, sCallable);
//        Assertions.assertNotNull(book);
//        Assertions.assertEquals(book.getNumber(), 34L);
//        Assertions.assertEquals(book.getName(), "xiyouji");
//        Assertions.assertEquals(book.getPrice(), "0.04");
//
//    }
//
//}
