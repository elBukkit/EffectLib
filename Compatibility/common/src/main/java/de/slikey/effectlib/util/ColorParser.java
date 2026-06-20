package de.slikey.effectlib.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

public class ColorParser {
    private Map<String, Color> colorMap;

    protected Color getColorByName(String name) {
        if (colorMap == null) {
            colorMap = new HashMap<>();
            colorMap.put("WHITE", Color.fromRGB(16777215));
            colorMap.put("SILVER", Color.fromRGB(12632256));
            colorMap.put("GRAY", Color.fromRGB(8421504));
            colorMap.put("BLACK", Color.fromRGB(0));
            colorMap.put("RED", Color.fromRGB(16711680));
            colorMap.put("MAROON", Color.fromRGB(8388608));
            colorMap.put("YELLOW", Color.fromRGB(16776960));
            colorMap.put("OLIVE", Color.fromRGB(8421376));
            colorMap.put("LIME", Color.fromRGB(65280));
            colorMap.put("GREEN", Color.fromRGB(32768));
            colorMap.put("AQUA", Color.fromRGB(65535));
            colorMap.put("TEAL", Color.fromRGB(32896));
            colorMap.put("BLUE", Color.fromRGB(255));
            colorMap.put("NAVY", Color.fromRGB(128));
            colorMap.put("FUCHSIA", Color.fromRGB(16711935));
            colorMap.put("PURPLE", Color.fromRGB(8388736));
            colorMap.put("ORANGE", Color.fromRGB(16753920));
        }

        return colorMap.get(name.toUpperCase());
    }

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
        if (value == null || value.isEmpty()) return null;
        if (value.startsWith("#")) value = value.substring(1);
        Color named = getColorByName(value);
        if (named != null) {
            return named;
        }
        int rgb;
        if (value.equalsIgnoreCase("random")) {
            byte red = (byte) (Math.random() * 255);
            byte green = (byte) (Math.random() * 255);
            byte blue = (byte) (Math.random() * 255);
            rgb = (red << 16) | (green << 8) | blue;
        } else {
            if (value.contains(",")) {
                String[] pieces = value.split(",");
                int red = Integer.parseInt(pieces[0]);
                int green = Integer.parseInt(pieces[0]);
                int blue = Integer.parseInt(pieces[0]);
                return Color.fromRGB(red, green, blue);
            } else {
                if (value.length() > 6) {
                    value = value.substring(0, 6);
                }
                rgb = Integer.parseInt(value, 16);
            }
        }
        return Color.fromRGB(rgb);
    }

    public Color fromARGB(int alpha, int red, int green, int blue) {
        return Color.fromRGB(red, green, blue);
    }

    public int getAlpha(Color color) {
        return 255;
    }
}
