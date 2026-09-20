package com.maybank.appsense.core.about;

import com.maybank.appsense.core.config.AppConstant;
import com.maybank.appsense.core.config.ResourcePropertiesUtil;
import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.AppInfoRecord;
import java.util.Properties;

public class AppInfo {

    private final AppInfoRecord info;

    public AppInfo() throws ConfigException {
        Properties build = ResourcePropertiesUtil.load(AppConstant.BUILD_INFO_RESOURCE);
        this.info = new AppInfoRecord(
                MessageUtil.get(MessageKey.APP_NAME),
                MessageUtil.get(MessageKey.APP_TAGLINE),
                build.getProperty(AppConstant.BUILD_VERSION_KEY),
                build.getProperty(AppConstant.BUILD_NUMBER_KEY),
                build.getProperty(AppConstant.BUILD_DATE_KEY),
                System.getProperty(AppConstant.JAVA_VERSION_PROPERTY),
                System.getProperty(AppConstant.OS_NAME_PROPERTY));
    }

    public AppInfoRecord get() {
        return info;
    }
}
