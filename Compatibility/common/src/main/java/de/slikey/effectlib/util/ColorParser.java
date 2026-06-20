package de.slikey.effectlib.util;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

public class ColorParser {
    public Color parse(ConfigurationSection configuration, String key) {
        if (configuration.isConfigurationSection(key)) {
            ConfigurationSection colorSection = configuration.getConfigurationSection(key);
            int red = colorSection.getInt("red");
            int green = colorSection.getInt("green");
            int blue = colorSection.getInt("blue");
            return Color.fromRGB(red, green, blue);
        }

        return parse(configuration.getString(key));
    }

    public Color parse(String value) {
        if (value == null) return null;
        int rgb;
        if (value.equalsIgnoreCase("random")) {
            byte red = (byte) (Math.random() * 255);
            byte green = (byte) (Math.random() * 255);
            byte blue = (byte) (Math.random() * 255);
            rgb = (red << 16) | (green << 8) | blue;
        } else {
            if (value.startsWith("#")) value = value.substring(1);
            rgb = Integer.parseInt(value, 16);
        }
        return Color.fromRGB(rgb);
    }
}
