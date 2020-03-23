package bma.photo.uploader.uploader;

public interface Uploader {

    void upload(UploaderRequest properties) throws UploaderException;

}
