package bma.photo.uploader.google;

import bma.photo.uploader.config.UploaderProperties;
import bma.photo.uploader.uploader.CredentialsAware;
import bma.photo.uploader.uploader.UploaderRequest;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.types.proto.Album;
import com.google.rpc.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class GooglePhotosUploaderTest {

//    private static final Pattern ALBUM_NAME_MASK = Pattern.compile("\\S/(\\d{4}-\\d{2}-\\d{2}.*|\\d{4}_\\d{2}_\\d{2}.*)/\\S");
    private static final Pattern ALBUM_NAME_MASK = Pattern.compile("\\S*/(\\d{4}-\\d{2}-\\d{2}.*|\\d{4}_\\d{2}_\\d{2}.*)/\\S*");

    @ParameterizedTest
    @CsvSource({
            "/foo/2020-01-02/, 2020-01-02",
            "/foo/2020_01_02/, 2020_01_02",
            "/foo/2020-01-02-aaa/, 2020-01-02-aaa",
            "/foo/2020_01_02 aaa/jpeg, 2020_01_02 aaa",
    })
    public void albumNameMatching(String folderPath, String albumName) {
        Matcher matcher = ALBUM_NAME_MASK.matcher(folderPath);
        Assertions.assertTrue(matcher.matches());
        Assertions.assertEquals(albumName, matcher.group(1));
    }

    @Mock
    private GooglePhotoApiClientFactory apiClientFactory;

    @Mock
    private PhotosLibraryClient client;

    private GooglePhotosUploader uploader;

    private Path workingDir;

    private static final String ALBUM_ID = "123";
    private static final String TOKEN1_ID = "t1";
    private static final String TOKEN2_ID = "t2";
    private static final String TOKEN3_ID = "t3";

    @BeforeEach
    public void setup()  {
        uploader = new GooglePhotosUploader(apiClientFactory);
    }

    @Test
    public void newAlbumCreated() throws IOException, URISyntaxException {
        workingDir = Path.of(GooglePhotosUploaderTest.class.getResource("/2020-03-10_empty").toURI());

        Mockito.when(apiClientFactory.create(Mockito.isA(CredentialsAware.class))).thenReturn(client);
        Mockito.when(client.createAlbum("2020-03-10_empty")).thenReturn(newTestAlbum(ALBUM_ID, "2020-03-10_empty"));

        uploader.upload(withUploaderRequest());

        Mockito.verify(client, Mockito.times(1)).createAlbum("2020-03-10_empty");
    }

    @Test
    public void photoFilesAddedIntoAlbum() throws URISyntaxException, IOException {
        workingDir = Path.of(GooglePhotosUploaderTest.class.getResource("/2020-03-10_full").toURI());

        Mockito.when(apiClientFactory.create(Mockito.isA(CredentialsAware.class))).thenReturn(client);
        Mockito.when(client.createAlbum("2020-03-10_full")).thenReturn(newTestAlbum(ALBUM_ID, "2020-03-10_full"));

        Mockito.when(client.uploadMediaItem(Mockito.isA(UploadMediaItemRequest.class))).thenReturn(
                mediaItemUploadingResult(TOKEN1_ID),
                mediaItemUploadingResult(TOKEN2_ID),
                mediaItemUploadingResult(TOKEN3_ID)
        );

        var uploadingMediaResults = List.of(
                mediaItemUploadResult("success"),
                mediaItemUploadResult("success"),
                mediaItemUploadResult("success"));

        var creatingMediaItemsResponse = BatchCreateMediaItemsResponse.newBuilder().addAllNewMediaItemResults(uploadingMediaResults).build();
        Mockito.when(client.batchCreateMediaItems(Mockito.eq(ALBUM_ID), Mockito.anyList())).thenReturn(creatingMediaItemsResponse);

        uploader.upload(withUploaderRequest());

        var captor = ArgumentCaptor.forClass(UploadMediaItemRequest.class);
        Mockito.verify(client, Mockito.times(3)).uploadMediaItem(captor.capture());
        Set<String> uploadingFileNames = captor.getAllValues().stream().map(UploadMediaItemRequest::getFileName).collect(Collectors.toSet());
        Assertions.assertEquals(Set.of("photo1.jpg", "photo2.jpg", "photo3.jpg"), uploadingFileNames);
    }

    @Test
    public void loadingAlbumIdFromPropertiesFile() throws URISyntaxException, IOException {
        workingDir = Path.of(GooglePhotosUploaderTest.class.getResource("/2020-03-10_album").toURI());

        Mockito.when(apiClientFactory.create(Mockito.isA(CredentialsAware.class))).thenReturn(client);
        Mockito.when(client.getAlbum(ALBUM_ID)).thenReturn(newTestAlbum(ALBUM_ID, "2020-03-10_album"));

        uploader.upload(withUploaderRequest());

        Mockito.verify(client, Mockito.times(1)).getAlbum(ALBUM_ID);
    }

    private UploaderRequest withUploaderRequest() {
        var uploaderProperties = new UploaderProperties();
        uploaderProperties.setFileExtensions(Set.of("jpg"));
        uploaderProperties.setFolderPath("jpeg");
        uploaderProperties.setCredentials(Map.of("accessToken", "1234567890"));

        return new UploaderRequest(uploaderProperties, workingDir);
    }

    private UploadMediaItemResponse mediaItemUploadingResult(String token) {
        return UploadMediaItemResponse.newBuilder().setUploadToken(token).build();
    }

    private NewMediaItemResult mediaItemUploadResult(String status) {
        return NewMediaItemResult.newBuilder().setStatus(Status.newBuilder().setMessage(status)).build();
    }

    private Album newTestAlbum(String id, String name) {
        return Album.newBuilder().setId(id).setTitle(name).build();
    }

}