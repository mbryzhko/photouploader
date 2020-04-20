package bma.photo.uploader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    @ConfigurationProperties
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }

}
