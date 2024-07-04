package com.shiroTest.config.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static com.shiroTest.config.JacksonConfig.setObjectMapper;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        setObjectMapper(objectMapper);
        return objectMapper;
    }

//    @Bean
//    public ObjectMapper objectMapper() {
//
//        return Jackson2ObjectMapperBuilder.json()
//                .modules(new JavaTimeModule())
//                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                .build();
//    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new CustomMappingJackson2HttpMessageConverter(objectMapper()));
    }
}

