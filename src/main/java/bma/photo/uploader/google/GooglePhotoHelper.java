package bma.photo.uploader.google;

import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.photos.types.proto.Album;
import com.google.rpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class GooglePhotoHelper {
    private static final Logger logger = LoggerFactory.getLogger(GooglePhotoHelper.class);

    private final PhotosLibraryClient client;

    public GooglePhotoHelper(PhotosLibraryClient client) {
        this.client = client;
    }

    public Album createNewAlbum(String title) {
        logger.info("Creating album: {}", title);
        Album album = client.createAlbum(title);
        logger.info("Album URL: {}", album.getProductUrl());
        return album;
    }

    public Album getAlbumById(String albumId) {
        return client.getAlbum(albumId);
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

        //  TODO: Print well-formatted result and handle unsuccessfully uploaded images.
        logger.info(result.toString());
    }

}
