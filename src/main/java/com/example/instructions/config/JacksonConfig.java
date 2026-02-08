package com.example.instructions.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public CsvMapper csvMapper() {
        return new CsvMapper();
    }
}
