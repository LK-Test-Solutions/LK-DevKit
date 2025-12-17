package org.opentdk.api.application;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.logging.Level;

public class BaseApplicationTest {
    @Test
    public void testApp() {
        String[] args = { "-logFile=tmp/app.log", "-settings=tmp/InheritedApp.json", "-traceLevel=INFO", "logEnabled=true", "-logArchiveSize=5" };
        InheritedApp app = new InheritedApp(args);
        InheritedSettings settings = new InheritedSettings();
        app.initLogger();
        try {
            settings = (InheritedSettings) app.initSettings(InheritedSettings.class);
        } catch (IOException e) {
            app.getLogger().log(Level.SEVERE, e.getMessage());
        }
        assert app.getProperties().size() == 5;
        assert app.getLogger() != null;
        assert app.getSettingsFile() != null;
        assert app.isLogEnabled();
        assert app.getSettings().getLogKeepAge() == 10;
        assert app.getSettings().getLogArchiveSize() == 5;
        assert settings.getCustomSetting().contentEquals("Test");
        assert settings.getLogKeepAge() == 10;
        assert settings.getLogArchiveSize() == 5;
        app.getLogger().log(Level.INFO, "App is started");
        app.saveSettings();
    }
}
