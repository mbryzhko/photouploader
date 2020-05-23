package bma.photo.uploader.uploader;

import bma.photo.uploader.config.UploaderProperties;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class UploaderRequest implements CredentialsAware {

    /**
     * Derived absolute path of folder with files.
     */
    @Getter
    private final Path uploadFolder;
    @Getter
    private final Set<String> fileExtensions;
    /**
     * Directory where the uploader application was executed from.
     */
    @Getter
    private final Path workingDir;
    @Getter
    private final Map<String, String> credentials;

    /**
     * Creates concrete uploader request based on {@link UploaderProperties} and working directory.
     * @param properties uploader configurations.
     * @param workingDir path of the uploader application run.
     */
    public UploaderRequest(UploaderProperties properties, Path workingDir) {
        this.fileExtensions = Collections.unmodifiableSet(properties.getFileExtensions());
        this.workingDir = workingDir;
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

    @Override
    public String toString() {
        return "UploaderRequest{" +
                "uploadFolder=" + uploadFolder +
                ", fileExtensions=" + fileExtensions +
                ", credentials=" + credentials +
                '}';
    }
}
