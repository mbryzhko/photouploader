package bma.photo.uploader.uploader.file;

import bma.photo.uploader.google.GoogleUploadingException;
import bma.photo.uploader.uploader.UploaderException;
import bma.photo.uploader.uploader.UploaderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public class FileHelper {
    private static final Logger logger = LoggerFactory.getLogger(FileHelper.class);

    private final Path folder;
    private final Set<String> extensions;

    public FileHelper(UploaderRequest request) {
        this.folder = Path.of(request.getUploadFolder().toUri());
        this.extensions = Collections.unmodifiableSet(request.getFileExtensions());
    }

    public void withFiles(Consumer<File> fileConsumer) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path : stream) {
                if (isPhotoFile(path)) {
                    fileConsumer.accept(path.toFile());
                }
            }
        } catch (IOException e) {
            throw new UploaderException("Error getting list of files in: " + folder.toString(), e);
        } catch (GoogleUploadingException e) {
            logger.warn(e.getMessage());
        }
    }

    private boolean isPhotoFile(Path filePath) {
        return Files.isRegularFile(filePath) && Files.isReadable(filePath) && hasRequestedExtension(filePath.getFileName().toString());
    }

    private boolean hasRequestedExtension(String fileName) {
        return extensions.stream().anyMatch(fileName::endsWith);
    }
}
