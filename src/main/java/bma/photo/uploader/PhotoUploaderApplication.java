package bma.photo.uploader;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class PhotoUploaderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PhotoUploaderApplication.class)
                .headless(false)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }
}
