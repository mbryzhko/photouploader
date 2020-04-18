package bma.photo.uploader.uploader;

/**
 * Thrown when uploader cannot finish uploading.
 */
public class UploaderException extends RuntimeException {

    public UploaderException(String message) {
        super(message);
    }

    public UploaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
