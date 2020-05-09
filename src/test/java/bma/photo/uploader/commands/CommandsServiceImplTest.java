package bma.photo.uploader.commands;

import bma.photo.uploader.config.ApplicationProperties;
import bma.photo.uploader.config.UploaderProperties;
import bma.photo.uploader.uploader.UploaderService;
import bma.photo.uploader.uploader.UploaderServiceProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandsServiceImplTest {

    private CommandsServiceImpl commandsService;

    private static final String UPLOAD_COMMAND = "upload";
    private static final String VERBOSE = "-v";

    @Mock
    private UploaderService uploaderService;

    @Mock
    private AlbumCommand albumCommand;

    private ApplicationProperties applicationProperties;

    @BeforeEach
    public void setup() {
        applicationProperties = new ApplicationProperties();
        commandsService = new CommandsServiceImpl(uploaderService, applicationProperties, albumCommand);
    }

    @Test
    public void uploadCommandStartsUploader() {
        var args = new String[]{UPLOAD_COMMAND};

        commandsService.handleCliArguments(args);

        Mockito.verify(uploaderService).runUpload(Mockito.isA(UploaderServiceProperties.class));
    }

    @Test
    public void uploaderServicePropertiesContainsListOfUploaders() {
        var args = new String[]{UPLOAD_COMMAND};
        appPropsWithGoogleUploader();
        ArgumentCaptor<UploaderServiceProperties> propertiesCaptor = ArgumentCaptor.forClass(UploaderServiceProperties.class);

        commandsService.handleCliArguments(args);

        Mockito.verify(uploaderService).runUpload(propertiesCaptor.capture());

        Assertions.assertEquals(applicationProperties.getUploaders(), propertiesCaptor.getValue().getUploaders());
    }

    @Test
    public void uploaderServicePropertiesContainsVerboseFromApplication() {
        var args = new String[]{UPLOAD_COMMAND};
        appPropsWithGoogleUploader();
        appPropsWithVerbose(true);
        ArgumentCaptor<UploaderServiceProperties> propertiesCaptor = ArgumentCaptor.forClass(UploaderServiceProperties.class);

        commandsService.handleCliArguments(args);

        Mockito.verify(uploaderService).runUpload(propertiesCaptor.capture());

        Assertions.assertTrue(propertiesCaptor.getValue().isVerbose());
    }

    @Test
    public void uploaderServicePropertiesContainsOverriddenVerboseFromCli() {
        var args = new String[]{VERBOSE, UPLOAD_COMMAND};
        appPropsWithGoogleUploader();
        appPropsWithVerbose(false);
        ArgumentCaptor<UploaderServiceProperties> propertiesCaptor = ArgumentCaptor.forClass(UploaderServiceProperties.class);

        commandsService.handleCliArguments(args);

        Mockito.verify(uploaderService).runUpload(propertiesCaptor.capture());

        Assertions.assertTrue(propertiesCaptor.getValue().isVerbose());
    }

    private void appPropsWithVerbose(boolean value) {
        applicationProperties.setVerbose(value);
    }

    @Test
    public void usageIsShownWhenNoCommandPassed() {
        var args = new String[]{};

        commandsService.handleCliArguments(args);

        Mockito.verify(uploaderService, Mockito.times(0)).runUpload(Mockito.isA(UploaderServiceProperties.class));
    }

    @Test
    public void showUsageAndErrorWhenIncorrectInput() {
        var args = new String[]{"album"};

        commandsService.handleCliArguments(args);

        Mockito.verify(albumCommand, Mockito.times(0)).run();

    }

    private void appPropsWithGoogleUploader() {
        applicationProperties.getUploaders().put("google", new UploaderProperties());
    }

}