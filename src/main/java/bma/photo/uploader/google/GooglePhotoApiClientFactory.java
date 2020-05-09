package bma.photo.uploader.google;

import bma.photo.uploader.uploader.CredentialsAware;
import bma.photo.uploader.uploader.UploaderException;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GooglePhotoApiClientFactory {

    private static final String OAUTH_ACCESS_TOKEN = "accessToken";

    public PhotosLibraryClient create(CredentialsAware credentials) throws IOException {
        return PhotosLibraryClient.initialize(getPhotosLibrarySettings(getAccessToken(credentials)));
    }

    private AccessToken getAccessToken(CredentialsAware request) {
        String accessTokenVal = request.getCredentials().get(OAUTH_ACCESS_TOKEN);
        return new AccessToken(accessTokenVal, null);
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
}
