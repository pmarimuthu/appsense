package com.maybank.appsense.gui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.maybank.appsense.gui.GuiConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** Loads SVG icons from the GUI resource tree. Icons are cached per name+size. */
public final class AppIcon {

    private static final Logger LOG = LoggerFactory.getLogger(AppIcon.class);
    private static final Map<String, Icon> CACHE = new HashMap<>();

    /**
     * Returns a toolbar-sized SVG icon by base name (without path or extension).
     * Returns an empty {@link ImageIcon} if the resource is not found.
     */
    public static Icon toolbar(String name) {
        return get(name, GuiConstant.TOOLBAR_ICON_SIZE);
    }

    /**
     * Returns an SVG icon at the given pixel size.
     */
    public static Icon get(String name, int size) {
        String cacheKey = name + "@" + size;
        return CACHE.computeIfAbsent(cacheKey, k -> load(name, size));
    }

    private static Icon load(String name, int size) {
        String path = GuiConstant.ICON_RESOURCE_BASE + name + ".svg";
        URL url = AppIcon.class.getResource(path);
        if (url == null) {
            LOG.debug("Icon resource not found: {}", path);
            return new ImageIcon(new java.awt.image.BufferedImage(size, size,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB));
        }
        return new FlatSVGIcon(url).derive(size, size);
    }

    /** Returns the application logo as an {@link Image} for window icon use. */
    public static Image appImage() {
        String path = "/com/maybank/appsense/gui/logo.svg";
        URL url = AppIcon.class.getResource(path);
        if (url == null) {
            return new java.awt.image.BufferedImage(32, 32,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
        }
        FlatSVGIcon svg = new FlatSVGIcon(url).derive(32, 32);
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(32, 32,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        svg.paintIcon(null, img.createGraphics(), 0, 0);
        return img;
    }

    private AppIcon() {
    }
}
