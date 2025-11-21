package org.opentdk.api.application;

/**
 * Storage object for the settings used by the {@link BaseApplication}. The user can extend it and add more settings.
 */
public class AppSettings {

	private String traceLevel = "INFO";
	private int logKeepAge = 10;
	private long logFileSize = 10 * 1024 * 1024;
	private int logArchiveSize = 10;
	private String appLanguage = "en";

    public String getTraceLevel() {
        return traceLevel;
    }

    public void setTraceLevel(String traceLevel) {
        this.traceLevel = traceLevel;
    }

    public int getLogKeepAge() {
        return logKeepAge;
    }

    public void setLogKeepAge(int logKeepAge) {
        this.logKeepAge = logKeepAge;
    }

    public long getLogFileSize() {
        return logFileSize;
    }

    public void setLogFileSize(long logFileSize) {
        this.logFileSize = logFileSize;
    }

    public int getLogArchiveSize() {
        return logArchiveSize;
    }

    public void setLogArchiveSize(int logArchiveSize) {
        this.logArchiveSize = logArchiveSize;
    }

    public String getAppLanguage() {
        return appLanguage;
    }

    public void setAppLanguage(String appLanguage) {
        this.appLanguage = appLanguage;
    }
}
