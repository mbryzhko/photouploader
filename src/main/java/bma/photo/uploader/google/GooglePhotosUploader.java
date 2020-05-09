package bma.photo.uploader.google;

import bma.photo.uploader.uploader.CredentialsAware;
import bma.photo.uploader.uploader.Uploader;
import bma.photo.uploader.uploader.UploaderException;
import bma.photo.uploader.uploader.UploaderRequest;
import bma.photo.uploader.uploader.file.FileHelper;
import com.google.api.gax.rpc.UnauthenticatedException;
import com.google.photos.types.proto.Album;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.groupingBy;

/**
 * Google Photos uploader that uses API to upload photos.
 * <br/>
 * Required Scope: https://www.googleapis.com/auth/photoslibrary.
 *
 * {@see https://developers.google.com/photos/library/guides/overview}
 */
@Service("google")
public class GooglePhotosUploader implements Uploader {

    private static final Logger logger = LoggerFactory.getLogger(GooglePhotosUploader.class);

    private static final Pattern ALBUM_NAME_MASK = Pattern.compile("\\S*/(\\d{4}-\\d{2}-\\d{2}.*|\\d{4}_\\d{2}_\\d{2}.*)/\\S*");
    private static final int MAX_UPLOAD_BATCH_SIZE = 50; // Google Photos API Restriction.

    private final GooglePhotoApiClientFactory apiClientFactory;

    @Autowired
    public GooglePhotosUploader(GooglePhotoApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    @Override
    public void upload(UploaderRequest request) {
        logger.info("Starting Google Photo Uploader with request: {}", request);
        
        withGooglePhotoHelper(request, helper -> {

            Album album = helper.createNewAlbum(deriveAlbumName(request));

            List<String> mediaItems = new ArrayList<>();
            new FileHelper(request).withFiles(file -> {
                mediaItems.add(helper.uploadMedia(file));
                if (mediaItems.size() >= MAX_UPLOAD_BATCH_SIZE) {
                    helper.addMediaItemsIntoAlbum(mediaItems, album.getId());
                    mediaItems.clear();
                }
            });

            if (mediaItems.size() > 0) {
                helper.addMediaItemsIntoAlbum(mediaItems, album.getId());
            }
        });

    }

    private String deriveAlbumName(UploaderRequest request) {
        String folderPathStr = request.getUploadFolder().toString();

        // Windows OS
        folderPathStr = folderPathStr.replace('\\', '/');

        Matcher matcher = ALBUM_NAME_MASK.matcher(folderPathStr);
        if (!matcher.matches() || matcher.groupCount() != 1) {
            throw new RuntimeException("Album name cannot be derived from path: " + folderPathStr);
        }

        return matcher.group(1);
    }

    public void withGooglePhotoHelper(CredentialsAware credentials, Consumer<GooglePhotoHelper> withHelper) {
        try (var photosLibraryClient = apiClientFactory.create(credentials)) {
            withHelper.accept(new GooglePhotoHelper(photosLibraryClient));
        } catch (UnauthenticatedException e) {
            throw new UploaderException(e.getMessage(), e);
        } catch (IOException e) {
            throw new UploaderException("Error initializing API client. " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new UploaderException("Unexpected Error. " + e.getMessage(), e);
        }

    }
}
