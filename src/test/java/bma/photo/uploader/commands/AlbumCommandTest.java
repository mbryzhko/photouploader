package bma.photo.uploader.commands;

import bma.photo.uploader.config.ApplicationProperties;
import bma.photo.uploader.config.UploaderProperties;
import bma.photo.uploader.google.GooglePhotoApiClientFactory;
import bma.photo.uploader.uploader.CredentialsAware;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.types.proto.Album;
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
    private AlbumCommand command;
    private ApplicationProperties properties;
    
    @Mock
    private GooglePhotoApiClientFactory apiClientFactory;

    @Mock
    private PhotosLibraryClient client;
    
    @BeforeEach
    public void setup() {
        properties = new ApplicationProperties();
        command = new AlbumCommand(apiClientFactory, properties);    
    }
    
    @Test
    public void albumInfoIsRetrievedById() throws IOException {
        command.setAlbumId(ALBUM_ID);

        var credentials = propertiesWithCredentials();

        Mockito.when(apiClientFactory.create(Mockito.isA(CredentialsAware.class))).thenReturn(client);
        Mockito.when(client.getAlbum("abc")).thenReturn(Album.newBuilder().setId(ALBUM_ID).build());

        command.run();

        ArgumentCaptor<CredentialsAware> credentialsCaptor = ArgumentCaptor.forClass(CredentialsAware.class);
        Mockito.verify(apiClientFactory, Mockito.only()).create(credentialsCaptor.capture());
        Assertions.assertEquals(credentials, credentialsCaptor.getValue().getCredentials());
    }

    private Map<String, String> propertiesWithCredentials() {
        Map<String, String> credentials = Map.of("accessToken", "1234567890");

        var uploaderProps = new UploaderProperties();
        uploaderProps.setCredentials(credentials);

        properties.setUploaders(Map.of(GOOGLE_PHOTO_RROPS_NAME, uploaderProps));

        return credentials;
    }


}