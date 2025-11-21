package org.opentdk.api.logger;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;

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

}
