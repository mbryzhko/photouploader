package bma.photo.uploader.config;

import bma.photo.uploader.PhotoUploaderApplication;
import bma.photo.uploader.google.GooglePhotosUploader;
import bma.photo.uploader.uploader.UploaderService;
import bma.photo.uploader.uploader.UploaderServiceImpl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig implements ApplicationContextAware {

    private ApplicationContext ctx;

    @Bean
    public UploaderService uploaderService(ApplicationProperties applicationProperties) {
        return new UploaderServiceImpl(applicationProperties);
    }

    @Bean
    public bma.photo.uploader.uploader.Uploader google() {
        return new GooglePhotosUploader();
    }

    @Bean
    public CommandLineRunner commandLineRunner(UploaderService uploaderService) {
        return args -> {
            uploaderService.runUpload();
            SpringApplication.exit(ctx, () -> 0);
        };
    }

    @Bean
    @ConfigurationProperties
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
}
