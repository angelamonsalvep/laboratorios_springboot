package com.riwi.thymeleaf_test.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {
    
    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        
        // Cargar variables del .env en las variables de sistema
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}

