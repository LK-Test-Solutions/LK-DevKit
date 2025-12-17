package org.opentdk.api.logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

/**
 * Class to initialize log handler instances to add them to the logger instance in the calling classes.
 *
 * @author FME
 */
public final class LogHandlerFactory {
    /**
     * @return {@link ConsoleHandler} instance with custom settings (like formatter)
     */
    public static ConsoleHandler buildConsoleHandler() {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new LogFormatter());
        return ch;
    }

    /**
     * @return @return {@link FileHandler} instance with custom settings (like formatter)
     * @name the corresponding logger name to add the handler to
     * @throws SecurityException
     * @throws IOException
     */
    public static FileHandler buildFileHandler(String loggerName) throws SecurityException, IOException {
        FileHandler fh = new FileHandler(loggerName, true);
        fh.setFormatter(new LogFormatter());
        return fh;
    }

    /**
     * Initializes log handlers for the specified logger. This method removes any existing handlers
     * from the logger, adds a `ConsoleHandler`, and optionally adds a `FileHandler` if `writeToFile`
     * is `true`. The log file is created if it does not already exist.
     *
     * @param logger the `Logger` instance to configure
     * @param writeToFile a flag indicating whether to log messages to a file
     */
    public static void initLogHandlers(Logger logger, boolean writeToFile) {
        logger.setUseParentHandlers(false);
        for(Handler handler : logger.getHandlers()) {
            handler.close();
            logger.removeHandler(handler);
        }
        logger.addHandler(LogHandlerFactory.buildConsoleHandler());

        if(writeToFile) {
            try {
                Path logFile = Paths.get(logger.getName());
                Files.createDirectories(logFile.getParent());
                if(Files.notExists(logFile)) {
                    Files.createFile(logFile);
                }
                logger.addHandler(LogHandlerFactory.buildFileHandler(logger.getName()));
            } catch (SecurityException | IOException e) {
                logger.log(Level.SEVERE, "Log file handler initialization failed. No log to file possible.");
            }
        }
    }
}