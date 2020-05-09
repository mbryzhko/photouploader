package bma.photo.uploader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

public class ApplicationProperties {

    public static final String GOOGLE_PHOTO_RROPS_NAME = "google";

    private Map<String, UploaderProperties> uploaders = new HashMap<>();
    private String workingDirectory = null;
    private boolean verbose = false;

    public Map<String, UploaderProperties> getUploaders() {
        return uploaders;
    }

    public void setUploaders(Map<String, UploaderProperties> uploaders) {
        this.uploaders = uploaders;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public boolean isWorkingDirectoryDefined() {
        return workingDirectory != null;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String toString() {
        return "ApplicationProperties{" +
                "uploaders=" + uploaders +
                '}';
    }
}
