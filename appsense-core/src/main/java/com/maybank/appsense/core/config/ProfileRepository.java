package com.maybank.appsense.core.config;

import com.maybank.appsense.core.exception.ConfigCorruptException;
import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.core.model.ProfilesFileBean;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileRepository {

    private static final Logger log = LoggerFactory.getLogger(ProfileRepository.class);

    private final Path file;
    private final Path backup;
    private boolean restoredFromBackup;

    public ProfileRepository(Path homeDir) {
        this.file = homeDir.resolve(AppConstant.PROFILES_FILE);
        this.backup = homeDir.resolve(AppConstant.PROFILES_BAK);
    }

    public List<ProfileBean> load() throws ConfigCorruptException {
        restoredFromBackup = false;
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            return read(file);
        } catch (IOException ex) {
            log.error("Profiles file {} could not be read", file, ex);
            return loadBackup(ex);
        }
    }

    public void save(List<ProfileBean> profiles) throws ConfigException {
        ProfilesFileBean bean = new ProfilesFileBean();
        bean.setVersion(AppConstant.JSON_SCHEMA_VERSION);
        bean.setProfiles(profiles);
        try {
            SafeFileWriterUtil.write(file, backup, JsonMapperUtil.mapper().writeValueAsBytes(bean));
        } catch (IOException ex) {
            log.error("Profiles file {} could not be written", file, ex);
            throw new ConfigException(MessageKey.ERROR_CONFIG_IO, ex, file.toString());
        }
    }

    public boolean isRestoredFromBackup() {
        return restoredFromBackup;
    }

    private List<ProfileBean> loadBackup(IOException primaryFailure) throws ConfigCorruptException {
        if (!Files.exists(backup)) {
            throw new ConfigCorruptException(file.toString(), primaryFailure);
        }
        try {
            List<ProfileBean> restored = read(backup);
            restoredFromBackup = true;
            return restored;
        } catch (IOException ex) {
            log.error("Profiles backup {} could not be read", backup, ex);
            throw new ConfigCorruptException(backup.toString(), ex);
        }
    }

    private List<ProfileBean> read(Path source) throws IOException {
        return JsonMapperUtil.mapper().readValue(source.toFile(), ProfilesFileBean.class).getProfiles();
    }
}
