package bma.photo.uploader.config;

import java.util.Map;
import java.util.Set;

public class UploaderProperties {

    private String folderPath;

    private Set<String> fileExtensions;

    private Map<String, String> credentials;

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public Set<String> getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(Set<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        return "UploaderProperties{" +
                "folderPath='" + folderPath + '\'' +
                ", fileExtensions=" + fileExtensions +
                '}';
    }
}
