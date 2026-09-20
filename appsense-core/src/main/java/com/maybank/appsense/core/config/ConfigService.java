package com.maybank.appsense.core.config;

import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.exception.ProfileNotFoundException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.core.model.SettingsBean;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfigService {

    static {
        LoggingBootstrapUtil.useBundledConfig();
    }

    private final Path homeDir;
    private final ProfileRepository profileRepository;
    private final SettingsRepository settingsRepository;
    private final List<String> startupWarnings = new ArrayList<>();
    private List<ProfileBean> profiles = new ArrayList<>();
    private SettingsBean settings;

    public ConfigService(Path homeDir, ProfileRepository profileRepository, SettingsRepository settingsRepository) {
        this.homeDir = homeDir;
        this.profileRepository = profileRepository;
        this.settingsRepository = settingsRepository;
    }

    /** Creates a service rooted at the default home folder under the user profile. */
    public static ConfigService createDefault() {
        Path home = Paths.get(System.getProperty(AppConstant.USER_HOME_PROPERTY), AppConstant.HOME_FOLDER_NAME);
        return new ConfigService(home, new ProfileRepository(home), new SettingsRepository(home));
    }

    /** Creates the home folder if needed, loads settings and profiles, and starts logging and i18n. */
    public void init() throws ConfigException {
        try {
            Files.createDirectories(homeDir.resolve(AppConstant.LOG_FOLDER));
        } catch (IOException ex) {
            throw new ConfigException(MessageKey.ERROR_CONFIG_IO, ex, homeDir.toString());
        }
        settings = settingsRepository.load();
        LoggingBootstrapUtil.apply(homeDir, settings);
        MessageUtil.init(Locale.forLanguageTag(settings.getLanguage()));
        profiles = profileRepository.load();
        collectWarnings();
    }

    public synchronized List<ProfileBean> getProfiles() {
        return profiles;
    }

    public synchronized ProfileBean findProfile(String name) throws ProfileNotFoundException {
        return profiles.stream()
                .filter(profile -> profile.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new ProfileNotFoundException(name));
    }

    public synchronized void saveProfile(ProfileBean profile) throws ConfigException {
        profiles.removeIf(existing -> existing.getName().equals(profile.getName()));
        profiles.add(profile);
        profileRepository.save(profiles);
    }

    public synchronized void deleteProfile(String name) throws ConfigException {
        profiles.removeIf(existing -> existing.getName().equals(name));
        profileRepository.save(profiles);
    }

    public SettingsBean getSettings() {
        return settings;
    }

    public synchronized void saveSettings() throws ConfigException {
        settingsRepository.save(settings);
    }

    public Path getHomeDir() {
        return homeDir;
    }

    public Path getLogDir() {
        return homeDir.resolve(AppConstant.LOG_FOLDER);
    }

    public List<String> getStartupWarnings() {
        return List.copyOf(startupWarnings);
    }

    private void collectWarnings() {
        if (settingsRepository.isRestoredFromBackup()) {
            startupWarnings.add(MessageUtil.get(MessageKey.ERROR_CONFIG_CORRUPT, AppConstant.SETTINGS_FILE));
        }
        if (settingsRepository.isDefaultsUsed()) {
            startupWarnings.add(MessageUtil.get(MessageKey.WARN_CONFIG_DEFAULTS, AppConstant.SETTINGS_FILE));
        }
        if (profileRepository.isRestoredFromBackup()) {
            startupWarnings.add(MessageUtil.get(MessageKey.ERROR_CONFIG_CORRUPT, AppConstant.PROFILES_FILE));
        }
    }
}
