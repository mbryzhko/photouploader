package bma.photo.uploader.google;

/**
 * Internal {@link GooglePhotosUploader} exception.
 */
public class GoogleUploadingException extends RuntimeException {

    public GoogleUploadingException(String message) {
        super(message);
    }

    public GoogleUploadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
