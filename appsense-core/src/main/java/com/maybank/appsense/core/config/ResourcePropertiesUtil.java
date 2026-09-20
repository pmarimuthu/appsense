package com.maybank.appsense.core.config;

import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.i18n.MessageKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourcePropertiesUtil {

    private static final Logger log = LoggerFactory.getLogger(ResourcePropertiesUtil.class);

    private ResourcePropertiesUtil() {
    }

    public static Properties load(String resource) throws ConfigException {
        try (InputStream stream = ResourcePropertiesUtil.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new ConfigException(MessageKey.ERROR_CONFIG_IO, null, resource);
            }
            Properties properties = new Properties();
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            return properties;
        } catch (IOException ex) {
            log.error("Failed to read resource {}", resource, ex);
            throw new ConfigException(MessageKey.ERROR_CONFIG_IO, ex, resource);
        }
    }
}
