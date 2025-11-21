/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2022, LK Test Solutions GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.opentdk.api.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class with static utility methods to work with date, time and time stamp formats using the {@link java.time} package.
 *
 * @author LK Test Solutions
 *
 */
public class DateUtil {

    /**
     * The {@link ZoneId} that gets used in the date and time utility functions of the <code>DateUtil</code> class. The default is
     * <code>ZoneId.systemDefault()</code> which is 'UTC+02:00' for Berlin (summer-time) and 'UTC+1' for winter time.
     */
    private static ZoneId zoneId = ZoneId.systemDefault();

    /**
     * Set a new time zone by committing a string with the UTC value e.g. 'UTC+01:00'.
     *
     * @param zone {@link #zoneId}
     */
    public static void setZoneId(String zone) {
        zoneId = ZoneId.of(zone);
    }

    /**
     * List of all supported date formats. Can be enriched by {@link #addPattern(String)}.
     */
    private static List<String> formats = Arrays.asList(
            "dd.MM.yyyy",
            // Dates
            "yyyyMMdd",              // Kompaktformat ohne Trenner
            "yyMMdd",
            "yyyy-MM-dd",            // ISO-Standardformat
            "yy-MM-dd",
            "yyyy-M-d",
            "yy-M-d",
            "yyyy.MM.dd",            // Punkte als Trenner (technisch)
            "yy.MM.dd",
            "yyyy.M.d",
            "yy.M.d",
            "yyyy/MM/dd",             // Technisch mit Slash
            "yy/MM/dd",
            "yyyy/M/d",
            "yy/M/d",
            "dd-MM-yyyy",            // Europäisches Standardformat
            "dd-MM-yy",
            "d-M-yyyy",
            "d-M-yy",
            "dd.MM.yyyy",            // Deutsches Standardformat
            "dd.MM.yy",
            "d.M.yyyy",
            "d.M.yy",
            "dd/MM/yyyy",            // Britisches Format
            "dd/MM/yy",
            "dd-MMM-yyyy",           // Tag, abgekürzter Monat, Jahr
            "dd-MMM-yy",
            "MM/dd/yyyy",            // US-Format
            "MM/dd/yy",
            "E, MMM dd yyyy",        // Abgekürzter Wochentag, Monat, Tag, Jahr
            "EEEE, MMMM dd, yyyy",   // Vollständiger Wochentag, Monat, Tag, Jahr
            "MMMM dd, yyyy",         // Vollständiger Monat, Tag, Jahr
            "EEE, d MMM yyyy",       // Abgekürzter Wochentag, Tag, abgekürzter Monat, Jahr
            "yyyy-'W'ww-u",           // ISO Woche-Jahr-Tag
            "yyyyMMdd-HHmmss",
            // Times
            "HH:mm",                 // Stunden:Minuten (24-Stunden-Format)
            "hh:mm a",               // Stunden:Minuten AM/PM (12-Stunden-Format)
            "HH:mm:ss",              // Stunden:Minuten:Sekunden (24-Stunden-Format)
            "HH.mm.ss",
            "hh:mm:ss a",            // Stunden:Minuten:Sekunden AM/PM (12-Stunden-Format)
            "HH:mm:ss.SSS",          // Stunden:Minuten:Sekunden.Millisekunden
            "HH:mm:ss.SSSSSS",       // Stunden:Minuten:Sekunden.Mikrosekunden
            "HH:mm:ss.SSSSSSSSS",    // Stunden:Minuten:Sekunden.Nanosekunden
            "h:mm a",                // Einzeldigit-Stunden, Minuten AM/PM
            "HH:mm:ss z",            // Stunden:Minuten:Sekunden mit Zeitzone
            "HH:mm:ss Z",            // Stunden:Minuten:Sekunden mit numerischer Zeitzone
            "HH:mm:ss.SSS XXX",       // Stunden:Minuten:Sekunden.Millisekunden mit ISO 8601 Zeitzone
            "HHmmss",
            // Date times
            "dd.MM.yyyy HH",
            "dd.MM.yyyy HH:mm:ss",
            "dd.MM.yyyy HH:mm:ss.SS",
            "dd.MM.yyyy HH:mm:ss.SSS",
            "dd.MM.yyyy HH:mm:ss.SSSSSSSSS",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss.SSS",
            "dd-MM-yyyy HH:mm:ss.SSSSSSSSS",
            "dd.MM.yyyy HH:mm:ss:SS",

            "yyyy-MM-dd HH:mm:ss",              // ISO-Datum mit Zeit
            "yyyy-MM-dd'T'HH:mm:ss",            // ISO 8601 Standard
            "yyyy-MM-dd'T'HH:mm:ss.SSS",        // ISO 8601 mit Millisekunden
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",     // ISO 8601 mit Millisekunden und Zeitzone
            "dd/MM/yyyy HH:mm",                 // Britisches Datum mit Zeit
            "MM/dd/yyyy hh:mm a",               // US-Datum mit 12-Stunden-Zeit
            "EEE, MMM d, ''yy h:mm a",          // Abgekürzter Wochentag, Monat, Jahr, Zeit
            "yyyy.MM.dd G 'at' HH:mm:ss z",     // ISO mit Ära und Zeitzone
            "EEE, d MMM yyyy HH:mm:ss Z",       // HTTP-Datum
            "MMM dd, yyyy, HH:mm:ss a",
            "yyyyMMddHHmmss",                   // Kompaktformat Datum und Zeit
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",        // ISO-Standard mit Zeitzone
            "yyyy.MM.dd HH:mm:ss",
            "yyyyMMdd HH:mm:ss",
            "yyyy-MM-dd-HH.mm.ss.SSSSSS",
            "yyyy.MM.dd HH:mm:ss.SSSSSSSSS",
            "yyyy-MM-dd.HH:mm:ss",
            "yyyy-MM-dd-HH:mm:ss,SSS",
            "yyyy.MM.dd HH:mm:ss.SSS",
            "yyyy-MM-dd.HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSS",
            // Specials
            "G yyyy-MM-dd",           // Ära (AD/BC) mit Datum
            "yyyy-MM-dd'T'HH:mm:ssXXX", // ISO-Format mit Offset-Zeitzone
            "yyyy-MM-dd'T'HH:mm:ss.SSSVV", // ISO-Format mit vollständigem Zeitzonennamen
            "w yyyy",                 // Kalenderwoche und Jahr
            "Q yyyy",                 // Quartal und Jahr
            "D yyyy",                 // Tag des Jahres und Jahr
            "D",                 		// Tag des Jahres
            "'Today is' EEEE",        // Benutzerdefinierter Text mit Wochentag
            "yyyy GGGG",              // Jahr mit vollständiger Ära
            "e yyyy-MM-dd",           // Lokalisierter Wochentag und Datum
            "u yyyy-MM-dd"            // ISO-Wochentag (1=Montag)
    );

    /**
     * @return {@link #formats}
     */
    public static List<String> getAllFormats() {
        return formats;
    }

    /**
     * @return all available formats of DateUtil as pipe separated string in brackets for regular expression checks.
     */
    public static String getAllFormatsAsRegex() {
        StringBuilder ret = new StringBuilder("(");
        for(int index = 0; index < formats.size(); index++) {
            ret.append(formats.get(index));
            if(index < formats.size() - 1) {
                ret.append("|");
            }
        }
        ret.append(")");
        return ret.toString();
    }

    /**
     * @param format pattern to add to the {@link #formats} list during runtime. Allows to take this format into account
     *               when using DateUtil
     */
    public static void addPattern(String format) {
        formats.add(format);
    }

    /**
     * Compares to strings as date, time or time stamp.
     *
     * @param dateTime        first comparison string.
     * @param compareDateTime second comparison string.
     * @return 0 = both instants are equal; -1 = first instant is before second; 1 = first instant is after second.
     */
    public static int compare(String dateTime, String compareDateTime) {
        ZonedDateTime first = retrieveZonedDateTime(retrieveTemporal(dateTime));
        ZonedDateTime second = retrieveZonedDateTime(retrieveTemporal(compareDateTime));
        if (first.isBefore(second)) {
            return -1;
        } else if (first.isAfter(second)) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Get the day difference between two instants as integer value. Turn of the year will be taken into account as well. E.g. getDayDiff("20193112",
     * "20200101") would return 1.
     *
     * @param dateTime        The first comparison input string.
     * @param compareDateTime The second comparison string.
     * @param type            the unit that will be compared e.g. days or seconds.
     * @return A positive integer value, or 0 if the instants are similar and -1 when no instant format could be detected.
     */
    public static int diff(String dateTime, String compareDateTime, ChronoUnit type) {
        ZonedDateTime first = retrieveZonedDateTime(retrieveTemporal(dateTime));
        ZonedDateTime second = retrieveZonedDateTime(retrieveTemporal(compareDateTime));
        // Ensure comparison between the correct instance types
        switch(type) {
            case NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, HALF_DAYS -> {
                first = first.toLocalDateTime().atZone(zoneId);
                second = second.toLocalDateTime().atZone(zoneId);
            }
            case DAYS, WEEKS, MONTHS, YEARS, DECADES, CENTURIES, MILLENNIA, ERAS -> {
                first = first.toLocalDate().atStartOfDay(zoneId);
                second = second.toLocalDate().atStartOfDay(zoneId);
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        }
        return (int) Math.abs(type.between(first, second));
    }

    /**
     * Retrieves the current date, time or time stamp.
     *
     * @param format The preferred date/time format e.g. yyyyMMdd.
     * @return The current instant in the committed format as string.
     */
    public static String get(String format) {
        ZonedDateTime instant = LocalDateTime.now().atZone(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(instant);
    }

    /**
     * Retrieves the date, time or time stamp depending on the input instant.
     *
     * @param dateTime A valid date, time or time stamp string.
     * @param format   The preferred date/time format e.g. yyyyMMdd.
     * @return The formatted/transformed string or an empty string if no instant could be retrieved.
     */
    public static String get(String dateTime, String format) {
        ZonedDateTime instant = retrieveZonedDateTime(retrieveTemporal(dateTime));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(instant);
    }

    /**
     * Retrieves the current date.
     *
     * @param millis The milliseconds from the epoch of 1970-01-01T00:00:00Z.
     * @param format The preferred date/time format e.g. yyyyMMdd.
     * @return The detected date as string in the default time zone.
     */
    public static String get(long millis, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId));
    }

    /**
     * Retrieves the current date, time or time stamp shifted to the past or future.
     *
     * @param diff   The amount to travel. This depends on the committed unit.
     * @param format The preferred date/time format e.g. yyyyMMdd.
     * @param unit   The travel unit e.g. <code>ChronoUnit.DAYS</code>.
     * @return The resulting date string in the committed format.
     */
    public static String get(int diff, String format, ChronoUnit unit) {
        ZonedDateTime instant = LocalDateTime.now().atZone(zoneId);
        if (diff > 0) {
            instant = instant.plus(diff, unit);
        } else if (diff < 0) {
            instant = instant.minus(-diff, unit);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(instant);
    }

    /**
     * Retrieves the committed date, time or time stamp shifted to the past or future.
     *
     * @param dateTime A valid date, time or time stamp string.
     * @param diff     The amount to travel. This depends on the committed unit.
     * @param format   The preferred date/time format e.g. yyyyMMdd.
     * @param unit     The travel unit e.g. <code>ChronoUnit.DAYS</code>.
     * @return The resulting date/time string in the committed format.
     */
    public static String get(String dateTime, int diff, String format, ChronoUnit unit) {
        ZonedDateTime zonedInstant = retrieveZonedDateTime(retrieveTemporal(dateTime));
        if (diff > 0) {
            zonedInstant = zonedInstant.plus(diff, unit);
        } else if (diff < 0) {
            zonedInstant = zonedInstant.minus(-diff, unit);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(zonedInstant);
    }

    /**
     * Retrieves the first date or time stamp depending on the committed type.
     *
     * @param type   The field to use e.g. <code>ChronoField.DAY_OF_WEEK</code> gets the first day of the week.
     * @param format The preferred date/time format e.g. yyyyMMdd.
     * @return The resulting date/time string in the committed format.
     */
    public static String getFirstOf(ChronoField type, String format) {
        ZonedDateTime instant = LocalDateTime.now().atZone(zoneId);
        instant = instant.with(type, instant.range(type).getMinimum());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(instant);
    }

    /**
     * Retrieves the first date or time stamp depending on the committed type.
     *
     * @param dateTime A valid date, time or time stamp string.
     * @param type     The field to use e.g. <code>ChronoField.DAY_OF_WEEK</code> gets the first day of the week.
     * @param format   The preferred date/time format e.g. yyyyMMdd.
     * @return The resulting date/time string in the committed format.
     */
    public static String getFirstOf(String dateTime, ChronoField type, String format) {
        ZonedDateTime instant = retrieveZonedDateTime(retrieveTemporal(dateTime));
        instant = instant.with(type, instant.range(type).getMinimum());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(instant);
    }

    /**
     * Retrieves the first date or time stamp depending on the committed type with the possibility to travel to the past or future.
     *
     * @param type   The field to use e.g. <code>ChronoField.DAY_OF_WEEK</code> gets the first day of the week.
     * @param format The preferred date/time format e.g. yyyyMMdd.
     * @param unit   The travel unit e.g. <code>ChronoUnit.DAYS</code>.
     * @param diff   The amount to travel. This depends on the committed unit.
     * @return The resulting date/time string in the committed format.
     */
    public static String getFirstOf(ChronoField type, String format, ChronoUnit unit, int diff) {
        return getFirstOf(get(diff, format, unit), type, format);
    }

    /**
     * Retrieves the first date or time stamp depending on the committed type with the possibility to travel to the past or future.
     *
     * @param dateTime A valid date, time or time stamp string.
     * @param type     The field to use e.g. <code>ChronoField.DAY_OF_WEEK</code> gets the first day of the week.
     * @param format   The preferred date/time format e.g. yyyyMMdd.
     * @param unit     The travel unit e.g. <code>ChronoUnit.DAYS</code>.
     * @param diff     The amount to travel. This depends on the committed unit.
     * @return The resulting date/time string in the committed format.
     */
    public static String getFirstOf(String dateTime, ChronoField type, String format, ChronoUnit unit, int diff) {
        return getFirstOf(get(dateTime, diff, format, unit), type, format);
    }

    /**
     * Retrieves the last date or time stamp depending on the committed type.
     *
     * @param type   The field to use e.g. <code>ChronoField.DAY_OF_WEEK</code> gets the first day of the week.
     * @param format The preferred date/time format e.g. yyyyMMdd.
     * @return The resulting date/time string in the committed format.
     */
    public static String getLastOf(ChronoField type, String format) {
        ZonedDateTime instant = LocalDateTime.now().atZone(zoneId);
        instant = instant.with(type, instant.range(type).getMaximum());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(instant);
    }

    /**
     * Retrieves the last date or time stamp depending on the committed type.
     *
     * @param dateTime A valid date, time or time stamp string.
     * @param type     The field to use e.g. <code>ChronoField.DAY_OF_WEEK</code> gets the first day of the week.
     * @param format  The preferred date/time format e.g. yyyyMMdd.
     * @return The resulting date/time string in the committed format.
     */
    public static String getLastOf(String dateTime, ChronoField type, String format) {
        ZonedDateTime instant = retrieveZonedDateTime(retrieveTemporal(dateTime));
        instant = instant.with(type, instant.range(type).getMaximum());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(instant);
    }

    /**
     * Retrieves the last date or time stamp depending on the committed type with the possibility to travel to the past or future.
     *
     * @param type   The field to use e.g. <code>ChronoField.DAY_OF_WEEK</code> gets the first day of the week.
     * @param format The preferred date/time format e.g. yyyyMMdd.
     * @param unit   The travel unit e.g. <code>ChronoUnit.DAYS</code>.
     * @param diff   The amount to travel. This depends on the committed unit.
     * @return The resulting date/time string in the committed format.
     */
    public static String getLastOf(ChronoField type, String format, ChronoUnit unit, int diff) {
        return getLastOf(get(diff, format, unit), type, format);
    }

    /**
     * Retrieves the last date or time stamp depending on the committed type with the possibility to travel to the past or future.
     *
     * @param dateTime A valid date, time or time stamp string.
     * @param type     The field to use e.g. <code>ChronoField.DAY_OF_WEEK</code> gets the first day of the week.
     * @param format   The preferred date/time format e.g. yyyyMMdd.
     * @param unit     The travel unit e.g. <code>ChronoUnit.DAYS</code>.
     * @param diff     The amount to travel. This depends on the committed unit.
     * @return The resulting date/time string in the committed format.
     */
    public static String getLastOf(String dateTime, ChronoField type, String format, ChronoUnit unit, int diff) {
        return getLastOf(get(dateTime, diff, format, unit), type, format);
    }

    /**
     * Retrieves the number of the part of the committed date, time or time stamp depending on the committed type.
     *
     * @param dateTime A valid date, time or time stamp string.
     * @param type     The field to use e.g. <code>ChronoField.DAY_OF_WEEK</code> gets the first day of the week.
     * @return The number as integer value.
     */
    public static int getNumber(String dateTime, ChronoField type) {
        return retrieveZonedDateTime(retrieveTemporal(dateTime)).get(type);
    }

    /**
     * This method gets a date string out of the committed string (exact match or containment).
     *
     * @param inStr   The input string that is a date string or contains one.
     * @param pFormat A valid date format
     * @return The detected date string or an empty string, if nothing could be found.
     */
    public static String parse(String inStr, String pFormat) {
        String parsedDate = "";
        Pattern p = Pattern.compile(pFormat);
        Matcher mDate = p.matcher(inStr);
        if (mDate.find()) {
            parsedDate = inStr.substring(mDate.start(), mDate.end());
        }
        return parsedDate;
    }

    /**
     * Method that gets the current milliseconds, counted from 1st Jan. 1970 and returns the length. The logarithm to the base 10 returns e.g. 3 when the
     * value 1000 is passed, 4 when the value 10000 is passed etc. So one digit has to be added. Another example: The value log10(111) is a bit higher
     * than 2 and the value log10(999) is a bit lower than 3. But the integer cast always returns 2 in this case (round down) and the result of this
     * method is 3, which is correct for both numbers.
     *
     * @return an integer value with the length of the milliseconds.
     */
    public static int getLengthMilliseconds() {
        long millis = System.currentTimeMillis();
        return (int) (Math.log10(millis) + 1);
    }

    /**
     * Gets the date from a given age. If the age is given as in interval separated with a minus, the function get a random date between the given ages.
     *
     * @param age    the age to be found as a date. To give an interval of ages, the ages must be separated with a minus within the string.
     * @param format valid date format.
     * @return the date of the given age or a random age between the given age interval.
     */
    public static String getDateFromAge(String age, String format) {
        String outDate = "";
        Random random = new Random();
        String[] a_age = { age };
        if (age.contains("-")) {
            a_age = age.split("-");
        }
        switch (a_age.length) {
            case 1 -> outDate = "'" + get(-(Integer.parseInt(a_age[0]) * 365), format, ChronoUnit.DAYS) + "'";
            case 2 -> outDate = "'" + get(-(random.nextInt(Integer.parseInt(a_age[1]) * 365 - Integer.parseInt(a_age[0]) * 365) + Integer.parseInt(a_age[0]) * 365), format, ChronoUnit.DAYS) + "'";
            case 3 -> outDate = "'" + age + "'";
        }
        return outDate;
    }

    /**
     * Gets an age in years from a given date.
     *
     * @param date the date where the age is calculated from.
     * @return the age in years as integer value.
     */
    public int getAgeFromDate(String date) {
        return diff(get(date, "yyyy-MM-dd"), get("yyyy-MM-dd"), ChronoUnit.YEARS);
    }

    
    
    /**
     * Method to search a date in a string.
     *
     * @param input string that may contain a date/time
     * @return the detected date/time or empty
     */
    public static Optional<String> findDate(String input) {
        String bestMatch = null;
        int highestScore = 0;

        for (String format : formats) {
            String regex = convertDateFormatToRegex(format);
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String foundDate = matcher.group();
                if(foundDate.length() == format.length() && !format.contentEquals("D")) {
//                    ZonedDateTime.parse(foundDate, DateTimeFormatter.ofPattern(format).withZone(zoneId));
                    int currentScore = calculateMatchScore(input, foundDate, format);
                    if (currentScore > highestScore) {
                        highestScore = currentScore;
                        bestMatch = foundDate;
                    }
                }
            }
        }
        return Optional.ofNullable(bestMatch);
    }
    
    public static Optional<String> findDate(String input, String format) {
        String bestMatch = null;
        int highestScore = 0;

        String regex = convertDateFormatToRegex(format);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String foundDate = matcher.group();
            if(foundDate.length() == format.length() && !format.contentEquals("D")) {
                int currentScore = calculateMatchScore(input, foundDate, format);
                if (currentScore > highestScore) {
                    highestScore = currentScore;
                    bestMatch = foundDate;
                }
            }
        }
        return Optional.ofNullable(bestMatch);
    }

    /**
     * Builds a regular expression for the given format, dynamically supporting all cases in the
     * {@link #formats} list. Handles literal text enclosed in single quotes and properly matches
     * variable digit lengths.
     *
     * @param dateFormat e.g., dd.MM.yyyy
     * @return A regex for the given format, e.g., \d{2}\.\d{2}\.\d{4}
     */
    public static String convertDateFormatToRegex(String dateFormat) {
        Map<Character, String> formatToRegex = getCharacterStringMap();

        // Special characters that need escaping in regex
        Map<Character, String> specialCharacters = new HashMap<>();
        specialCharacters.put('.', "\\.");
        specialCharacters.put('-', "-");
        specialCharacters.put('/', "/");
        specialCharacters.put(':', ":");
        specialCharacters.put(',', ",");

        StringBuilder regexPattern = new StringBuilder();
        boolean inLiteral = false;
        for (int i = 0; i < dateFormat.length(); i++) {
            char c = dateFormat.charAt(i);
            if (c == '\'') { // Toggle literal text handling
                inLiteral = !inLiteral;
                continue;
            }
            if (inLiteral) {
                regexPattern.append(Pattern.quote(String.valueOf(c))); // Append literal character
            } else if (formatToRegex.containsKey(c)) {
                regexPattern.append(formatToRegex.get(c)); // Append regex for format character
            } else if (specialCharacters.containsKey(c)) {
                regexPattern.append(specialCharacters.get(c)); // Append escaped special character
            } else {
                regexPattern.append(Pattern.quote(String.valueOf(c))); // Append unknown text as literal
            }
        }
        return regexPattern.toString();
    }

    private static Map<Character, String> getCharacterStringMap() {
        Map<Character, String> formatToRegex = new HashMap<>();
        formatToRegex.put('d', "\\d{1}");       // Day of month
        formatToRegex.put('M', "\\d{1}");       // Month
        formatToRegex.put('y', "\\d{1}");       // Year
        formatToRegex.put('H', "\\d{1}");       // Hour (24-hour clock)
        formatToRegex.put('h', "\\d{1}");       // Hour (12-hour clock)
        formatToRegex.put('m', "\\d{1}");       // Minute
        formatToRegex.put('s', "\\d{1}");       // Second
        formatToRegex.put('S', "\\d{1}");       // Fractional seconds
        formatToRegex.put('D', "\\d{1}");       // Day of year
        formatToRegex.put('E', "\\w{3,}");        // Day of week (abbreviated or full)
        formatToRegex.put('W', "\\d{1}");         // Week in the month
        formatToRegex.put('w', "\\d{1}");       // Week of year
        formatToRegex.put('Q', "\\d{1}");         // Quarter of year
        formatToRegex.put('G', "\\w+");           // Era
        formatToRegex.put('u', "\\d{1}");         // Day number of week (1-7)
        formatToRegex.put('z', "[A-Za-z/]+");     // Time zone names
        formatToRegex.put('X', "[+-]\\d{2}(:?\\d{2})?"); // ISO Time zone
        formatToRegex.put('\'', "");              // Escaped single quote (literal text)
        return formatToRegex;
    }

    /**
     * Gets a {@link java.time.temporal.TemporalAccessor} object out of a date string to pass it to te {@link #retrieveZonedDateTime(TemporalAccessor)} method.
     * @param dateTime input date in all available formats
     * @return {@link java.time.temporal.TemporalAccessor}
     * @throws IllegalArgumentException if all formats got checked without result
     */
    public static TemporalAccessor retrieveTemporal(String dateTime) {
        for(String pattern : getAllFormats()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            try {
                return formatter.parse(dateTime);
            } catch(DateTimeParseException e) {
                continue;
            }
        }
        throw new DateTimeException("Format not supported ==> " + dateTime);
    }
    
    public static boolean isTemporal(String dateTime) {
        for(String pattern : getAllFormats()) {
        	if(!pattern.contentEquals("D")) {
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
	            try {
	                formatter.parse(dateTime);
	                return true;
	            } catch(DateTimeParseException e) {
	                continue;
	            }
        	}
        }
        return false;
    }

    /**
     * Calculates a match score based on how well a detected date matches the input string and its
     * format. Higher scores indicate better matches, considering proximity and length of the
     * detected match.
     *
     * @param input        The input string that contains the date.
     * @param detectedDate The detected date substring.
     * @param format       The format used to parse the date.
     * @return The match score as an integer.
     */
    public static int calculateMatchScore(String input, String detectedDate, String format) {
        // Higher score for closer matches
        int closenessScore = Math.abs(input.length() - detectedDate.length());

        // Higher score for a date format that matches more precisely in length
        int formatMatch = Math.abs(format.length() - detectedDate.length());

        // Combining scores, with negative impact for mismatched length
        return (1000 - closenessScore) - formatMatch;
    }

    public static ZonedDateTime retrieveZonedDateTime(TemporalAccessor temporal) {
        ZonedDateTime ret;
        try {
            ret = LocalDateTime.from(temporal).atZone(zoneId);
        } catch (DateTimeException e) {
            ret = null;
        }
        if(ret == null) {
            try {
                ret = LocalDate.from(temporal).atStartOfDay(zoneId);
            } catch(DateTimeException e) {
                ret = null;
            }
        }
        if(ret == null) {
            try {
                ret = LocalTime.from(temporal).atDate(LocalDate.now()).atZone(zoneId);
            } catch (DateTimeException e) {
                ret = null;
            }
        }
        // Last try for special formats
        if(ret == null) {
            int year = 0;
            int week = 1;
            int dayOfWeek = 1;
            if(temporal.isSupported(ChronoField.YEAR)) {
                year = temporal.get(ChronoField.YEAR);
            }
            if(temporal.isSupported(WeekFields.ISO.weekOfYear())) {
                week = (int) temporal.getLong(WeekFields.ISO.weekOfYear());
            }
            if(temporal.isSupported(WeekFields.ISO.dayOfWeek())) {
                dayOfWeek = (int) temporal.getLong(WeekFields.ISO.dayOfWeek());
            }
            ret = LocalDate.of(year, 1, 1).with(WeekFields.ISO.weekOfYear(), week).with(WeekFields.ISO.dayOfWeek(), dayOfWeek).atStartOfDay(zoneId);
        }
        return ret;
    }
}