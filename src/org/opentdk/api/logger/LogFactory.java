package org.opentdk.api.logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to initialize log instances with predefined behavior. This includes logging to console and file.
 *
 * @author FME
 */
public final class LogFactory {
    /**
     * @return {@link java.util.logging.Logger} with custom settings.
     */
    public static Logger buildLogger(Path logFile, String traceLevel, boolean writeToFile) {
        Logger logger = Logger.getLogger(logFile.toString());
        Level level;
        try {
            level = Level.parse(traceLevel);
        } catch (IllegalArgumentException e) {
            level = Level.INFO;
        }
        logger.setLevel(level);
        LogHandlerFactory.initLogHandlers(logger, writeToFile);

        return logger;
    }
}