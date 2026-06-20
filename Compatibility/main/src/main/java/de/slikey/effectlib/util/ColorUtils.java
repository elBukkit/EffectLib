package de.slikey.effectlib.util;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

import de.slikey.effectlib.util.versions.ColorParser_20_5;

public class ColorUtils {
    private static ColorUtils instance;
    private final ColorParser parser;

    public static void initialize() {
        instance = new ColorUtils();
    }

    public static Color parse(ConfigurationSection configuration, String key) {
        return instance.parser.parse(configuration, key);
    }

    public static Color parse(String value) {
        return instance.parser.parse(value);
    }

    public static Color fromARGB(int alpha, int red, int green, int blue) {
        return instance.parser.fromARGB(alpha, red, green, blue);
    }

    private ColorUtils() {
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
