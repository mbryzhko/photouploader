package bma.photo.uploader.uploader;

import bma.photo.uploader.config.UploaderProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class UploaderRequest implements CredentialsAware {

    private final Path uploadFolder;
    private final Set<String> fileExtensions;
    private final Map<String, String> credentials;

    public UploaderRequest(UploaderProperties properties, Path workingDir) {
        this.fileExtensions = Collections.unmodifiableSet(properties.getFileExtensions());
        this.uploadFolder = resolveUploadFolder(properties, workingDir);
        this.credentials = Collections.unmodifiableMap(properties.getCredentials());
    }

    private Path resolveUploadFolder(UploaderProperties properties, Path workingDir) {
        Path folder = workingDir.resolve(properties.getFolderPath());
        if (!Files.isDirectory(folder) || !Files.isReadable(folder)) {
            throw new UploaderException("Upload folder: " + folder + " should be a directory and read access");
        }
        return folder;
    }

    public Path getUploadFolder() {
        return uploadFolder;
    }

    public Set<String> getFileExtensions() {
        return fileExtensions;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    @Override
    public String toString() {
        return "UploaderRequest{" +
                "uploadFolder=" + uploadFolder +
                ", fileExtensions=" + fileExtensions +
                ", credentials=" + credentials +
                '}';
    }
}
