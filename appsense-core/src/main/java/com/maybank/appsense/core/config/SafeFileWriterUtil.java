package com.maybank.appsense.core.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class SafeFileWriterUtil {

    private SafeFileWriterUtil() {
    }

    /**
     * Writes content using the temp, backup, rename sequence so a crash leaves either the old file
     * or the backup intact.
     */
    public static void write(Path target, Path backup, byte[] content) throws IOException {
        Path temp = target.resolveSibling(target.getFileName() + AppConstant.TMP_SUFFIX);
        Files.write(temp, content);
        if (Files.exists(target)) {
            Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
