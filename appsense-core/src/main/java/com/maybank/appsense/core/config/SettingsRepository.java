package com.maybank.appsense.core.config;

import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.model.SettingsBean;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsRepository {

    private static final Logger log = LoggerFactory.getLogger(SettingsRepository.class);

    private final Path file;
    private final Path backup;
    private boolean restoredFromBackup;
    private boolean defaultsUsed;

    public SettingsRepository(Path homeDir) {
        this.file = homeDir.resolve(AppConstant.SETTINGS_FILE);
        this.backup = homeDir.resolve(AppConstant.SETTINGS_BAK);
    }

    public SettingsBean load() throws ConfigException {
        restoredFromBackup = false;
        defaultsUsed = false;
        if (!Files.exists(file)) {
            return defaults();
        }
        try {
            return overlay(file);
        } catch (IOException ex) {
            log.error("Settings file {} could not be read", file, ex);
            return loadBackup();
        }
    }

    public void save(SettingsBean settings) throws ConfigException {
        try {
            SafeFileWriterUtil.write(file, backup, JsonMapperUtil.mapper().writeValueAsBytes(settings));
        } catch (IOException ex) {
            log.error("Settings file {} could not be written", file, ex);
            throw new ConfigException(MessageKey.ERROR_CONFIG_IO, ex, file.toString());
        }
    }

    public boolean isRestoredFromBackup() {
        return restoredFromBackup;
    }

    public boolean isDefaultsUsed() {
        return defaultsUsed;
    }

    private SettingsBean loadBackup() throws ConfigException {
        if (Files.exists(backup)) {
            try {
                SettingsBean restored = overlay(backup);
                restoredFromBackup = true;
                return restored;
            } catch (IOException ex) {
                log.error("Settings backup {} could not be read", backup, ex);
            }
        }
        defaultsUsed = true;
        return defaults();
    }

    private SettingsBean overlay(Path source) throws IOException, ConfigException {
        return JsonMapperUtil.mapper().readerForUpdating(defaults()).readValue(source.toFile());
    }

    private SettingsBean defaults() throws ConfigException {
        Properties properties = ResourcePropertiesUtil.load(AppConstant.DEFAULT_SETTINGS_RESOURCE);
        Map<String, Object> values = new HashMap<>();
        properties.forEach((key, value) -> values.put(key.toString(), value));
        return JsonMapperUtil.mapper().convertValue(values, SettingsBean.class);
    }
}
