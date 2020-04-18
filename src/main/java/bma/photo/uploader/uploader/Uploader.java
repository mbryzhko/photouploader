package bma.photo.uploader.uploader;

/**
 * Uploader that takes {@link UploaderRequest} as an input and performs uploading to cloud storage.
 * <br/>
 * It also responsible for validation of file size.
 */
public interface Uploader {

    void upload(UploaderRequest properties) throws UploaderException;

}
