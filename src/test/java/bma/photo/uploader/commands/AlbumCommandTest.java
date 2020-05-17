package bma.photo.uploader.commands;

import bma.photo.uploader.config.ApplicationProperties;
import bma.photo.uploader.config.UploaderProperties;
import bma.photo.uploader.google.GooglePhotoApiClientFactory;
import bma.photo.uploader.uploader.CredentialsAware;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.types.proto.Album;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static bma.photo.uploader.config.ApplicationProperties.GOOGLE_PHOTO_RROPS_NAME;

@ExtendWith(MockitoExtension.class)
class AlbumCommandTest {

    public static final String ALBUM_ID = "abc";
    public static final String ALBUM_NAME = "name";

    private AlbumCommand command;
    private ApplicationProperties properties;
    
    @Mock
    private GooglePhotoApiClientFactory apiClientFactory;

    @Mock
    private PhotosLibraryClient client;

    @Mock
    private InternalPhotosLibraryClient.ListSharedAlbumsPagedResponse albums;
    
    @BeforeEach
    public void setup() {
        properties = new ApplicationProperties();
        command = new AlbumCommand(apiClientFactory, properties);    
    }

    @Test
    public void validationErrorWhenNeitherIdNorNameSpecified() throws IOException {

        command.run();

        Mockito.verify(apiClientFactory, Mockito.times(0)).create(Mockito.isA(CredentialsAware.class));
    }

    @Test
    public void albumInfoIsRetrievedById() throws IOException {
        command.setAlbumId(ALBUM_ID);
        var testAlbum = Mockito.spy(newTestAlbum(ALBUM_ID, ALBUM_NAME));

        var credentials = propertiesWithCredentials();

        Mockito.when(apiClientFactory.create(Mockito.isA(CredentialsAware.class))).thenReturn(client);
        Mockito.when(client.getAlbum(ALBUM_ID)).thenReturn(testAlbum);

        command.run();

        ArgumentCaptor<CredentialsAware> credentialsCaptor = ArgumentCaptor.forClass(CredentialsAware.class);
        Mockito.verify(apiClientFactory, Mockito.only()).create(credentialsCaptor.capture());
        Assertions.assertEquals(credentials, credentialsCaptor.getValue().getCredentials());

        Mockito.verify(testAlbum).getId();
        Mockito.verify(testAlbum).getTitle();
        Mockito.verify(testAlbum).getProductUrl();
        Mockito.verify(testAlbum).getShareInfo();
    }

    @Test
    public void albumInfoIsRetrievedByName() throws IOException {
        command.setAlbumName(ALBUM_NAME);
        var testAlbum = Mockito.spy(newTestAlbum(ALBUM_ID, ALBUM_NAME));

        var credentials = propertiesWithCredentials();
        Mockito.when(apiClientFactory.create(Mockito.isA(CredentialsAware.class))).thenReturn(client);
        Mockito.when(client.listSharedAlbums()).thenReturn(albums);
        Mockito.when(albums.iterateAll()).thenReturn(Lists.list(newTestAlbum("foo", "bar"), testAlbum));

        command.run();

        Mockito.verify(testAlbum).getId();
        Mockito.verify(testAlbum, Mockito.times(2)).getTitle();
        Mockito.verify(testAlbum).getProductUrl();
        Mockito.verify(testAlbum).getShareInfo();
    }

    private Album newTestAlbum(String id, String name) {
        return Album.newBuilder().setId(id).setTitle(name).build();
    }

    private Map<String, String> propertiesWithCredentials() {
        Map<String, String> credentials = Map.of("accessToken", "1234567890");

        var uploaderProps = new UploaderProperties();
        uploaderProps.setCredentials(credentials);

        properties.setUploaders(Map.of(GOOGLE_PHOTO_RROPS_NAME, uploaderProps));

        return credentials;
    }

}