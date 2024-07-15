package com.vlat.configuration;

import com.vlat.utils.CryptoTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestConfiguration {
    @Value("${utils.salt}")
    private String salt;

    @Bean
    public CryptoTool cryptoTool(){
        return new CryptoTool(salt);
    }
}
