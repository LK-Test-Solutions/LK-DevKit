package org.lk.devkit.util;

import java.nio.charset.Charset;

/**
 * Helper class with string methods for direct use in a static way.
 * 
 * @author LK Test Solutions
 */
public class StringUtil {

	/**
	 * @param toConvert Valid String object.
	 * @return The UNICODE transformation or null, if the input string is null, empty or too large.
	 */
	public static String toUnicode(String toConvert) {
		if(toConvert.isBlank() || toConvert.length() > Short.MAX_VALUE) {
			return null;
		}
		return new String(toConvert.getBytes(), Charset.defaultCharset());
	}

	/**
	 * @param inStr Valid String object.
	 * @return The regular expression transformation or null, if the input string is null, empty or too large.
	 */
	public static String stringToRegEx(String inStr) {
		if(inStr.isBlank() || inStr.length() > Short.MAX_VALUE) {
			return null;
		}		
		String regexStr = inStr.replace("\"", "\\\"");
		regexStr = regexStr.replaceAll("\\[", "\\\\[");
		regexStr = regexStr.replaceAll("\\]", "\\\\]");
		regexStr = regexStr.replaceAll("\\.", "\\\\.");
		regexStr = regexStr.replaceAll("\\*", "\\\\*");
		regexStr = regexStr.replaceAll("\\(", "\\\\(");
		regexStr = regexStr.replaceAll("\\)", "\\\\)");
		return regexStr;
	}

	/**
	 * @param inStr Valid String object.
	 * @return The literal expression transformation or null, if the input string is null, empty or too large.
	 */
	public static String regExToString(String inStr) {
		if(inStr.isBlank() || inStr.length() > Short.MAX_VALUE) {
			return null;
		}	
		String normalStr = inStr.replace("\\\"", "\"");
		normalStr = normalStr.replace("\\[", "[");
		normalStr = normalStr.replace("\\]", "]");
		normalStr = normalStr.replace("\\.", ".");
		normalStr = normalStr.replace("\\*", "*");
		normalStr = normalStr.replace("\\(", ")");
		normalStr = normalStr.replace("\\)", ")");
		return normalStr;
	}

	/**
	 * @param regex Valid String object.
	 * @return A string with all double backslashes replaced by single ones or null, if the input string is null, empty or too large.
	 */
	public static String replaceDoubleBackslashes(String regex) {
		if(regex.isBlank() || regex.length() > Short.MAX_VALUE) {
			return null;
		}	
		return regex.replaceAll("\\\\\\\\", "\\\\");
	}

	/**
	 * @param inStr Valid String object.
	 * @return A string with all enclosing quotes removed or null, if the input string is null, empty or too large.
	 */
	public static String removeEnclosingQuotes(String inStr) {
		if(inStr.isBlank() || inStr.length() > Short.MAX_VALUE) {
			return null;
		}
		String clearedString = inStr.replaceAll("\\A[ ]*\"", ""); // remove opening quotes
		clearedString = clearedString.replaceAll("\"[ ]*\\Z", ""); // remove closing quotes
		return clearedString;
	}

}
