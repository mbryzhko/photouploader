package bma.photo.uploader.uploader;

import bma.photo.uploader.config.ApplicationProperties;
import bma.photo.uploader.config.UploaderProperties;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains properties from {@link ApplicationProperties}
 * that maybe overridden by CLI arguments.
 */
@Builder
@Getter
public class UploaderServiceProperties {

    private Map<String, UploaderProperties> uploaders;

    private String workingDirectory;

    private boolean verbose = false;

}
