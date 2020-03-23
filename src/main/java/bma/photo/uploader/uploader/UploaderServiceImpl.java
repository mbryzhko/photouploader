package bma.photo.uploader.uploader;

import bma.photo.uploader.PhotoUploaderApplication;
import bma.photo.uploader.config.ApplicationProperties;
import bma.photo.uploader.config.UploaderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.nio.file.Path;
import java.nio.file.Paths;

public class UploaderServiceImpl implements UploaderService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(UploaderServiceImpl.class);

    private ApplicationProperties applicationProperties;

    private ApplicationContext applicationContext;

    public UploaderServiceImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void runUpload() {
        applicationProperties.getUploaders().forEach((name, prop) -> {
            logger.info("Initializing '{}' uploader", name);
            try {
                applicationContext.getBean(name, Uploader.class).upload(createUploadRequest(prop));
            } catch (UploaderException e ) {
                if (applicationProperties.isVerbose()) {
                    logger.error("Error uploading. ", e);
                } else {
                    logger.error("Error uploading. " + e.getMessage());
                }
            } catch (BeansException e) {
                logger.error("Error lookup for uploader: " + name);
            }
        });
    }

    private UploaderRequest createUploadRequest(UploaderProperties prop) {
        Path workingDir = Paths.get("").toAbsolutePath();

        if (applicationProperties.isWorkingDirectoryDefined()) {
            workingDir = Paths.get(applicationProperties.getWorkingDirectory());
        }

        return new UploaderRequest(prop, workingDir);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
