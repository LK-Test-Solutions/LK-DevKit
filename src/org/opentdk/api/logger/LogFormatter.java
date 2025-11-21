package org.opentdk.api.logger;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Class to have a custom log record as output. Has to be committed as <code>new LogFormatter()</code> to the handler(s) of the logger instance.
 * 
 * @author FME (LK Test Solutions)
 *
 */
public class LogFormatter extends SimpleFormatter {
	@Override
	public String format(LogRecord logRecord) {	
		// %x refers to the number of the following parameter
		// E.g [2025-01-01 12:00:00] [SEVERE] logs/application.log: Invalid program parameter 
		return String.format("[%1$tF %1$tT] [%2$s] %3$s: %4$s%n", logRecord.getMillis(), logRecord.getLevel().getName(), logRecord.getLoggerName(), logRecord.getMessage());
	}
}
