package org.opentdk.api.logger;

import org.opentdk.api.util.DateUtil;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Class to have a custom log record as output. Has to be committed as <code>new LogFormatter()</code> to the handler(s) of the logger instance.
 *
 * @author FME (LK Test Solutions)
 *
 */
public class LogFormatter extends Formatter {
	@Override
	public String format(LogRecord logRecord) {
		// Sample output: [2025-01-01 12:00:00.000] [Thread-1] [INFO] org.opentdk.api.logger.LogFormatter#format: This is a log message.

        StringBuilder ret = new StringBuilder();
		// Time stamp
		ret.append("[").append(DateUtil.get(logRecord.getMillis(), "yyyy-MM-dd HH:mm:ss.SSS")).append("] [");
		// Thread
		// ret.append("Thread-" + logRecord.getLongThreadID()).append(" ");
		ret.append("Thread-").append(Thread.currentThread().getId()).append("] [");
		// Level
		ret.append(logRecord.getLevel().getName()).append("] ");
		// Class
		if (logRecord.getSourceClassName() != null) {
			ret.append(logRecord.getSourceClassName()).append("#");
		}
		// Method
		if (logRecord.getSourceMethodName() != null) {
			ret.append(logRecord.getSourceMethodName()).append(": ");
		}
		// Message
		ret.append(formatMessage(logRecord));
		// Exception
		if (logRecord.getThrown() != null) {
			ret.append(logRecord.getThrown().getMessage());
		}
		// End of line
		ret.append(System.lineSeparator());

		return ret.toString();
	}
}
