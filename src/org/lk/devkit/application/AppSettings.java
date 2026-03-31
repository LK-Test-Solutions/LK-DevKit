package org.lk.devkit.application;

import lombok.Getter;
import lombok.Setter;

/**
 * Storage object for the settings used by the {@link BaseApplication}. The user can extend it and add more settings.
 */
@Getter @Setter
public class AppSettings {

    private String traceLevel = "INFO";
    private int logKeepAge = 10;
    private long logFileSize = 10 * 1024 * 1024;
    private int logArchiveSize = 10;
    private String appLanguage = "en";
}