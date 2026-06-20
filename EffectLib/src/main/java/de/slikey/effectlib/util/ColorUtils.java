package de.slikey.effectlib.util;

import java.util.logging.Logger;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

import de.slikey.effectlib.util.versions.ColorParser_20_5;

public class ColorUtils {
    private static ColorUtils instance;
    private static Logger logger;
    private final ColorParser parser;

    public static void initialize(Logger log) {
        logger = log;
        instance = new ColorUtils();
    }

    public static Color parse(ConfigurationSection configuration, String key) {
        try {
            return instance.parser.parse(configuration, key);
        } catch (Exception ex) {
            logger.warning("Invalid color configuration: " + configuration.get(key) + ": " + ex.getMessage());
            return null;
        }
    }

    public static Color parse(ConfigurationSection configuration, String key, Color defaultValue) {
        Color color = parse(configuration, key);
        return color == null ? defaultValue : color;
    }

    public static Color parse(String value) {
        try {
            return instance.parser.parse(value);
        } catch (Exception ex) {
            logger.warning("Invalid color string: " + value + ": " + ex.getMessage());
            return null;
        }
    }

    public static Color parse(String value, Color defaultValue) {
        Color color = parse(value);
        return color == null ? defaultValue : color;
    }

    public static Color fromARGB(int alpha, int red, int green, int blue) {
        return instance.parser.fromARGB(alpha, red, green, blue);
    }

    public static Color fromARGB(int alpha, int red, int green, int blue, Color defaultValue) {
        Color color = fromARGB(alpha, red, green, blue);
        return color == null ? defaultValue : color;
    }

    public static int getAlpha(Color color) {
        return instance.parser.getAlpha(color);
    }

    protected ColorUtils() {
        boolean hasAlpha = true;
        try {
            Color.class.getMethod("fromARGB", Integer.TYPE);
        } catch (Exception legacy) {
            hasAlpha = false;
        }
        if (hasAlpha) {
            parser = new ColorParser_20_5();
        } else {
            parser = new ColorParser();
        }
    }
}
