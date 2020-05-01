package bma.photo.uploader.commands;

import bma.photo.uploader.config.ApplicationProperties;
import bma.photo.uploader.uploader.UploaderService;
import bma.photo.uploader.uploader.UploaderServiceProperties;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CommandsServiceImpl implements CommandsService, CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CommandsServiceImpl.class);

    private final UploaderService uploaderService;
    private final ApplicationProperties applicationProperties;
    private UploadCommand uploadCommand;

    @Parameter(names = "-v", description = "Verbose")
    private boolean argVerbose;

    @Autowired
    public CommandsServiceImpl(UploaderService uploaderService, ApplicationProperties applicationProperties) {
        this.uploaderService = uploaderService;
        this.applicationProperties = applicationProperties;
        this.uploadCommand = new UploadCommand();

    }

    @Override
    public final void run(String... args) throws Exception {
        this.handleCliArguments(args);
    }

    @Override
    public void handleCliArguments(String[] args) {
        logger.debug(Arrays.toString(args));

        JCommander commandListParser = createCommandListParser(args);

        String parsedCommand = commandListParser.getParsedCommand();

        if (UploadCommand.NAME.equals(parsedCommand)) {
            uploadCommand.run();
        } else {
            commandListParser.usage();
        }
    }

    private JCommander createCommandListParser(String[] args) {
        JCommander result = JCommander.newBuilder()
                .acceptUnknownOptions(true)
                .addObject(this)
                .addCommand(uploadCommand)
                .build();

        result.parse(args);

        return result;
    }

    @Parameters(commandDescription = "Run uploading", commandNames = UploadCommand.NAME)
    private class UploadCommand {
        static final String NAME = "upload";

        public void run() {
            var builder = UploaderServiceProperties.builder()
                    .uploaders(applicationProperties.getUploaders())
                    .workingDirectory(applicationProperties.getWorkingDirectory())
                    .verbose(argVerbose || applicationProperties.isVerbose());

            uploaderService.runUpload(builder.build());
        }
    }
}
