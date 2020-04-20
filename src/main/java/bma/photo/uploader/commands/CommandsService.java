package bma.photo.uploader.commands;

/**
 * This class is responsible for parsing CLI arguments and running corresponding command.
 */
public interface CommandsService {

    void handleCliArguments(String[] args);

}
