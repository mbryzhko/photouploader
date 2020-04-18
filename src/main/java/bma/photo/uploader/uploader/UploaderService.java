package bma.photo.uploader.uploader;

/**
 * Main entry point into uploader service.
 */
public interface UploaderService {

    /**
     * Prepare properties and delegate uploading to concrete uploader.
     */
    void runUpload();

}
