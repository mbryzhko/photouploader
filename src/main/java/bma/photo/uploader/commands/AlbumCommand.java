package bma.photo.uploader.commands;

import bma.photo.uploader.config.ApplicationProperties;
import bma.photo.uploader.config.UploaderProperties;
import bma.photo.uploader.google.GooglePhotoApiClientFactory;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.types.proto.Album;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static bma.photo.uploader.config.ApplicationProperties.GOOGLE_PHOTO_RROPS_NAME;

@Component
@Parameters(commandDescription = "Get Album Info", commandNames = AlbumCommand.NAME)
public class AlbumCommand {
    protected static final String NAME = "album";

    private static final Logger logger = LoggerFactory.getLogger(AlbumCommand.class);

    @Parameter(names = "-id", description = "AlbumId", required = false)
    @Setter @Getter
    private String albumId;

    @Parameter(names = "-name", description = "AlbumName", required = false)
    @Setter @Getter
    private String albumName;


    private final GooglePhotoApiClientFactory apiClientFactory;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public AlbumCommand(GooglePhotoApiClientFactory apiClientFactory, ApplicationProperties applicationProperties) {
        this.apiClientFactory = apiClientFactory;
        this.applicationProperties = applicationProperties;
    }

    public void run() {
        boolean hasGoogleProps = applicationProperties.getUploaders().containsKey(GOOGLE_PHOTO_RROPS_NAME);

        if (albumId == null && albumName == null) {
            logger.error("Album 'id' or 'name' should be specified");
            return;
        }

        if (hasGoogleProps) {
            UploaderProperties googlePhotoProps = applicationProperties.getUploaders().get(GOOGLE_PHOTO_RROPS_NAME);
            try (PhotosLibraryClient client = apiClientFactory.create(googlePhotoProps::getCredentials)) {
                findAlbum(client).ifPresentOrElse(this::printAlbumDetails, () -> logger.error("Album not found"));
            } catch (IOException | ApiException e) {
                logger.error("Cannot print album info, error: {}", e.getMessage());
            }
        }
    }

    private void printAlbumDetails(Album album) {
        logger.info("Album General Info:");
        logger.info("  Id = {}", album.getId());
        logger.info("  Title = {}", album.getTitle());
        logger.info("  URL = {}", album.getProductUrl());
        logger.info("Album Sharing Info: {}", album.getShareInfo().getSharedAlbumOptions());
    }

    private Optional<Album> findAlbum(PhotosLibraryClient client)  {
        if (albumId != null) {
            try {
                logger.info("Search album by id: {}", albumId);
                return Optional.of(client.getAlbum(albumId));
            } catch (InvalidArgumentException e) {
                return Optional.empty();
            }
        } else if (albumName != null) {
            logger.info("Search album by name: {}", albumName);
            for (Album album : client.listSharedAlbums().iterateAll()) {
                if (album.getTitle().equalsIgnoreCase(albumName)) {
                    return Optional.of(album);
                }
            }
        }
        return Optional.empty();
    }
}
