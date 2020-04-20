package bma.photo.uploader.uploader;

import bma.photo.uploader.config.UploaderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UploaderServiceImpl implements UploaderService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(UploaderServiceImpl.class);

    private ApplicationContext applicationContext;

    @Override
    public void runUpload(UploaderServiceProperties properties) {
        properties.getUploaders().forEach((name, uploaderProperties) -> {
            logger.info("Initializing '{}' uploader", name);
            try {
                applicationContext.getBean(name, Uploader.class)
                        .upload(createUploadRequest(properties.getWorkingDirectory(), uploaderProperties));
            } catch (UploaderException e ) {
                if (properties.isVerbose()) {
                    logger.error("Error uploading. ", e);
                } else {
                    logger.error("Error uploading. " + e.getMessage());
                }
            } catch (BeansException e) {
                logger.error("Error lookup for uploader: " + name);
            }
        });
    }

    private UploaderRequest createUploadRequest(String workingDirectory, UploaderProperties prop) {
        Path workingDir = Paths.get("").toAbsolutePath();

        if (!StringUtils.isEmpty(workingDirectory)) {
            workingDir = Paths.get(workingDirectory);
        }

        return new UploaderRequest(prop, workingDir.toAbsolutePath());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
