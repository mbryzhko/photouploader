package bma.photo.uploader.commands;

import bma.photo.uploader.config.ApplicationProperties;
import bma.photo.uploader.config.UploaderProperties;
import bma.photo.uploader.google.GooglePhotoApiClientFactory;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.api.gax.rpc.ApiException;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.types.proto.Album;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static bma.photo.uploader.config.ApplicationProperties.GOOGLE_PHOTO_RROPS_NAME;

@Component
@Parameters(commandDescription = "Get Album Info", commandNames = AlbumCommand.NAME)
public class AlbumCommand {
    protected static final String NAME = "album";

    private static final Logger logger = LoggerFactory.getLogger(AlbumCommand.class);

    @Parameter(names = "-id", description = "AlbumId", required = true)
    @Setter @Getter
    private String albumId;

    private final GooglePhotoApiClientFactory apiClientFactory;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public AlbumCommand(GooglePhotoApiClientFactory apiClientFactory, ApplicationProperties applicationProperties) {
        this.apiClientFactory = apiClientFactory;
        this.applicationProperties = applicationProperties;
    }

    public void run() {

        boolean hasGoogleProps = applicationProperties.getUploaders().containsKey(GOOGLE_PHOTO_RROPS_NAME);

        if (hasGoogleProps) {
            UploaderProperties googlePhotoProps = applicationProperties.getUploaders().get(GOOGLE_PHOTO_RROPS_NAME);
            try (PhotosLibraryClient client = apiClientFactory.create(googlePhotoProps::getCredentials)) {
                Album album = client.getAlbum(albumId);

                logger.info("Album General Info: {}", album.toString());
                logger.info("Album Sharing Info: {}", album.getShareInfo().getSharedAlbumOptions());

            } catch (IOException | ApiException e) {
                logger.error("Cannot get album info, id: {}, error: {}", albumId, e.getMessage());
            }
        }
    }
}
