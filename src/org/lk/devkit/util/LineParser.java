package org.lk.devkit.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author FME (LK Test Solutions)
 */
public class LineParser {
	
	public enum ParseMode {
		WHOLE_LINE,
		LEFT_OF_MATCHING_STRING,
		MATCHING_STRING,
		RIGHT_OF_MATCHING_STRING,
		NONE;
	}
	
	// Handles one line in the source file and returns the search result depending on the settings
	public static String parseLine(String line, ParseMode mode, String regex, boolean includePattern) {
		StringBuilder res = new StringBuilder();
		
		Pattern pattern = Pattern.compile(regex, 2);
		Matcher matcher;
		if (!line.isEmpty()) {
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				
				switch (mode) {
				case WHOLE_LINE:
					res.append(line);
					break;
				case LEFT_OF_MATCHING_STRING:
					if (includePattern) {
						res.append(line, 0, matcher.end());
						break;
					}
					res.append(line, 0, matcher.start());
					break;
				case MATCHING_STRING:
					res.append(line, matcher.start(), matcher.end());
					break;
				case RIGHT_OF_MATCHING_STRING:
					if (includePattern) {
						res.append(line.substring(matcher.start()));
						break;
					}
					res.append(line.substring(matcher.end()));
					break;
				case NONE:
					res.append("None");
					break;
				default:
					throw new IllegalArgumentException("No parse mode set");
				}
			}
		}
		return res.toString();
	}
}
