package org.opentdk.api.logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
			
		logger.setUseParentHandlers(false);
		for (Handler handler : logger.getHandlers()) {
			handler.close();
			logger.removeHandler(handler);
		}
		// Use factory class to create a console handler that uses the custom formatter as well
		logger.addHandler(LogHandlerFactory.buildConsoleHandler());

		if(writeToFile) {
			try {
				Files.createDirectories(logFile.getParent());
				if(Files.notExists(logFile)) {
					Files.createFile(logFile);
				}
				logger.addHandler(LogHandlerFactory.buildFileHandler(logger.getName()));
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
		return logger;
	}
}
