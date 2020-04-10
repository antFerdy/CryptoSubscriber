package com.crypto.cryptosusbcriber.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonSerializerConfig {

    @Bean
    public ObjectMapper buildMapper() {
        return new ObjectMapper();
    }
}
