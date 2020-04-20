package bma.photo.uploader.google;

import bma.photo.uploader.uploader.Uploader;
import bma.photo.uploader.uploader.UploaderException;
import bma.photo.uploader.uploader.UploaderRequest;
import bma.photo.uploader.uploader.file.FileHelper;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.UnauthenticatedException;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.CreateAlbumRequest;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.photos.types.proto.Album;
import com.google.rpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
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
    private static final String OAUTH_ACCESS_TOKEN = "accessToken";
    private static final Pattern ALBUM_NAME_MASK = Pattern.compile("\\S*/(\\d{4}-\\d{2}-\\d{2}.*|\\d{4}_\\d{2}_\\d{2}.*)/\\S*");
    private static final int MAX_UPLOAD_BATCH_SIZE = 50; // Google Photos API Restriction.

    @Override
    public void upload(UploaderRequest request) {
        logger.info("Starting Google Photo Uploader with request: {}", request);

        AccessToken accessToken = getAccessToken(request);
        
        withGooglePhotoHelper(accessToken, helper -> {

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

    private AccessToken getAccessToken(UploaderRequest request) {
        String accessTokenVal = request.getCredentials().get(OAUTH_ACCESS_TOKEN);
        return new AccessToken(accessTokenVal, null);
    }

    public void withGooglePhotoHelper(AccessToken accessToken, Consumer<Helper> withHelper) {
        PhotosLibrarySettings settings = getPhotosLibrarySettings(accessToken);

        try (var photosLibraryClient = PhotosLibraryClient.initialize(settings)) {
            withHelper.accept(new Helper(photosLibraryClient));
        } catch (UnauthenticatedException e) {
            throw new UploaderException(e.getMessage(), e);
        } catch (IOException e) {
            throw new UploaderException("Error initializing API client. " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new UploaderException("Unexpected Error. " + e.getMessage(), e);
        }

    }

    private PhotosLibrarySettings getPhotosLibrarySettings(AccessToken accessToken) {
        try {
            var credProvider = FixedCredentialsProvider.create(OAuth2Credentials.create(accessToken));

            return PhotosLibrarySettings.newBuilder()
                    .setCredentialsProvider(credProvider)
                    .build();

        } catch (IOException e) {
            throw new UploaderException("Cannot create setting for Google Photo Client", e);
        }
    }

    private static class Helper {
        private final PhotosLibraryClient client;

        public Helper(PhotosLibraryClient client) {
            this.client = client;
        }

        public Album createNewAlbum(String title) {
            logger.info("Creating album: {}", title);
            return client.createAlbum(title);
        }

        public String uploadMedia(File photoFile) {
            logger.info("Uploading: " + photoFile);

            UploadMediaItemRequest uploadMediaRequest = UploadMediaItemRequest.newBuilder()
                    .setFileName(photoFile.getName())
                    .setDataFile(createRaFile(photoFile))
                    .build();

            UploadMediaItemResponse uploadMediaResponse;
            try {
                uploadMediaResponse = client.uploadMediaItem(uploadMediaRequest);
            } catch (RuntimeException e) {
                throw new GoogleUploadingException("Error uploading media: " + photoFile.toString(), e);
            }

            if (uploadMediaResponse.getError().isPresent() || uploadMediaResponse.getUploadToken().isEmpty()) {
                throw new GoogleUploadingException("Error uploading media: " + photoFile.toString(), uploadMediaResponse.getError().get().getCause());
            }

            return uploadMediaResponse.getUploadToken().get();
        }

        private RandomAccessFile createRaFile(File photoFile)  {
            try {
                return new RandomAccessFile(photoFile, "r");
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File cannot be found: " + photoFile.toString());
            }
        }

        public void addMediaItemsIntoAlbum(Collection<String> mediaItems, String albumId) {
            List<NewMediaItem> newItems = mediaItems.stream().map(NewMediaItemFactory::createNewMediaItem).collect(Collectors.toList());

            BatchCreateMediaItemsResponse response = client.batchCreateMediaItems(albumId, newItems);

            Map<Status, Long> result = response.getNewMediaItemResultsList().stream()
                    .collect(groupingBy(NewMediaItemResult::getStatus, counting()));

            logger.info(result.toString());

        }
    }
}
