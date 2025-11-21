/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2025, LK Test Solutions GmbH
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
package org.opentdk.api.application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import org.opentdk.api.logger.LogArchiver;
import org.opentdk.api.logger.LogFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.*;

@Getter
public abstract class BaseApplication {
    /**
     * Logger for the whole application.
     */
    private Logger logger;
    /**
     * File for the {@link #logger}.
     */
    private Path logFile = Paths.get("logs" + File.separator + getClass().getSimpleName() + ".log");
    /**
     * True: Logfile gets written, false: No logging at all.
     */
    private boolean logEnabled = true;
    /**
     * Storage object for all specific application settings.
     */
    private AppSettings settings;
    /**
     * File for the {@link #settings}.
     */
    private Path settingsFile = Paths.get("conf" + File.separator + getClass().getSimpleName() + ".json");
    /**
     * Storage object for all main application settings.
     */
    private Properties properties;

    /**
     * Entry point of the application. The program parameter get passed as "=" separated key value pairs.
     * E.g.
     * <pre>
     *     String[] args = { "-logFile=logs/app.log", "-settings=conf/settings.json", "-traceLevel=INFO" };
     *     InheritedApp app = new InheritedApp(args);
     * </pre>
     *
     * Where <b>InheritedApp</b> inherits from {@link BaseApplication}. The minus prefix is optional.
     */
    public BaseApplication(String[] args) {
        properties = new Properties();
        for (String arg : args) {
            if(arg.contains("=")) {
                String argKey = arg.split("=")[0];
                String value = arg.split("=")[1];
                if (argKey.startsWith("-")) {
                    argKey = argKey.replace("-", "");
                }
                properties.put(argKey, value);
            }
        }
    }

    /**
     * Sets up the {@link #logger}. If no 'logPath' was committed as program parameter, the default
     * path 'log/MainClass.log' gets used. By default, logging to file is enabled, but can be switched off
     * by using the 'logEnabled' program parameter.
     */
    public void initLogger() {
        if(properties.containsKey("logFile")) {
            logFile = Paths.get(properties.getProperty("logFile"));
        }
        if(properties.containsKey("logEnabled")) {
            logEnabled = Boolean.parseBoolean(properties.getProperty("logEnabled"));
        }
        String traceLevel = "INFO";
        if(properties.containsKey("traceLevel")) {
        	traceLevel = properties.getProperty("traceLevel");
        }
        logger = LogFactory.buildLogger(logFile, traceLevel, logEnabled);      
    }

    /**
     * Loads the JSON content from the {@link #settingsFile} into the {@link #settings}. If no program parameter 'settings'
     * was committed, the default 'conf/MainClass.json' gets used. Or the {@link #settingsFile} gets overwritten before calling this method.
     */
    public void initSettings() throws IOException {
        initSettings(AppSettings.class);
    }

    /**
     * Works like {@link #initSettings()} but with the possibility to use a custom settings class that inherits from {@link AppSettings}.
     */
    public AppSettings initSettings(Class<? extends AppSettings> settingsClass) throws IOException {
    	if(properties.containsKey("settings")) {
            settingsFile = Paths.get(properties.getProperty("settings"));
            logger.log(Level.INFO, "Settings file argument is set ==> " + settingsFile);
        } else {
            Files.createDirectories(settingsFile.getParent());
            settingsFile = Paths.get(settingsFile.getParent().toString(), settingsClass.getSimpleName() + ".json");
        }
        if(Files.notExists(settingsFile)) {
            logger.log(Level.INFO, "Settings file does not exist and gets created ==> " + settingsFile.toString());
            Files.createFile(settingsFile);
            Files.writeString(settingsFile, "{}");
        }
        try (FileReader reader = new FileReader(settingsFile.toFile())) {
        	settings = new GsonBuilder().setPrettyPrinting().create().fromJson(reader, settingsClass);
            logger.log(Level.INFO, "Settings file loaded into class ==> " + settingsClass.getSimpleName());
        }
        // Overwrite settings with program parameter if required
        mergeSettingsAndProps();
        
        long logSize = settings.getLogFileSize();
		int keepAge = settings.getLogKeepAge();
		int archiveSize = settings.getLogArchiveSize();
		try {
			LogArchiver.archiveIfNecessary(logFile, logSize, keepAge, archiveSize);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Log archive routine failed");
		}
		return settings;
    }

    private void mergeSettingsAndProps() {
        if(properties.containsKey("traceLevel")) {
            settings.setTraceLevel(properties.getProperty("traceLevel"));
        }
        if(properties.containsKey("logKeepAge")) {
            settings.setLogKeepAge(Integer.parseInt(properties.getProperty("logKeepAge")));
        }
        if(properties.containsKey("logFileSize")) {
            settings.setLogFileSize(Long.parseLong(properties.getProperty("logFileSize")));
        }
        if(properties.containsKey("logArchiveSize")) {
            settings.setLogArchiveSize(Integer.parseInt(properties.getProperty("logArchiveSize")));
        }
    }

    /**
     * Stores all changes that were made during runtime in the {@link #settingsFile} by loading it from the
     * {@link #settings} object.
     */
    public void saveSettings() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonContent = gson.toJson(settings);
        try {
            Files.writeString(settingsFile, jsonContent);
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }
}
