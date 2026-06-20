package de.slikey.effectlib.util.versions;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

import de.slikey.effectlib.util.ColorParser;

public class ColorParser_20_5 extends ColorParser {

    @Override
    public Color parse(ConfigurationSection configuration, String key) {
        if (configuration.isConfigurationSection(key)) {
            ConfigurationSection colorSection = configuration.getConfigurationSection(key);
            int red = colorSection.getInt("red");
            int green = colorSection.getInt("green");
            int blue = colorSection.getInt("blue");
            int alpha = colorSection.getInt("alpha", 255);
            return Color.fromARGB(alpha, red, green, blue);
        }

        return parse(configuration.getString(key));
    }

    @Override
    public Color parse(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (value.startsWith("#")) value = value.substring(1);
        Color named = getColorByName(value);
        if (named != null) {
            return named;
        }

        int rgba;
        if (value.equalsIgnoreCase("random")) {
            byte red = (byte) (Math.random() * 255);
            byte green = (byte) (Math.random() * 255);
            byte blue = (byte) (Math.random() * 255);
            byte alpha = (byte) 255;
            rgba = (alpha << 24) | (red << 16) | (green << 8) | blue;
        } else if (value.equalsIgnoreCase("arandom")) {
            byte red = (byte) (Math.random() * 255);
            byte green = (byte) (Math.random() * 255);
            byte blue = (byte) (Math.random() * 255);
            byte alpha = (byte) (Math.random() * 255);
            rgba = (alpha << 24) | (red << 16) | (green << 8) | blue;
        } else if (value.contains(",")) {
            String[] pieces = value.split(",");
            int red = Integer.parseInt(pieces[0]);
            int green = Integer.parseInt(pieces[0]);
            int blue = Integer.parseInt(pieces[0]);
            int alpha = 255;
            if (pieces.length > 3) {
                alpha = Integer.parseInt(pieces[0]);
            }
            return Color.fromARGB(alpha, red, green, blue);
        } else {
            if (value.length() == 6) value = "FF" + value;
            rgba = (int)Long.parseLong(value, 16);
        }
        return Color.fromARGB(rgba);
    }

    @Override
    public Color fromARGB(int alpha, int red, int green, int blue) {
        return Color.fromARGB(alpha, red, green, blue);
    }

    @Override
    public int getAlpha(Color color) {
        return color.getAlpha();
    }
}
