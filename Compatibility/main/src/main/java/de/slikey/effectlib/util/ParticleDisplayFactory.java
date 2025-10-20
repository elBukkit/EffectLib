package de.slikey.effectlib.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import de.slikey.effectlib.util.versions.ParticleDisplay_12;
import de.slikey.effectlib.util.versions.ParticleDisplay_13;
import de.slikey.effectlib.util.versions.ParticleDisplay_21_4;
import de.slikey.effectlib.util.versions.ParticleDisplay_21_10;

public class ParticleDisplayFactory {
    public static ParticleDisplay newInstance() {
        ParticleDisplay display;
        boolean hasColorTransition = false;
        boolean hasColorDataType = false;
        try {
            Class.forName("org.bukkit.Particle$Spell");
            display = new ParticleDisplay_21_10();
            hasColorTransition = true;
            hasColorDataType = true;
        } catch (ClassNotFoundException not21_10) {
            try {
                // @NotNull Particle var1, @NotNull Location var2, int var3, double var4, double var6, double var8, double var10, @Nullable T var12, boolean var13
                // particle, center, options.amount, options.offsetX, options.offsetY, options.offsetZ, options.speed, options.data, options.forceShow
                Player.class.getMethod("spawnParticle", Particle.class, Location.class, Integer.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Object.class, Boolean.TYPE);
                display = new ParticleDisplay_21_4();
                hasColorTransition = true;
                hasColorDataType = true;
            } catch (Throwable not21_4) {
                // TODO: This could all be cleaned up a bit.
                try {
                    Particle.valueOf("DUST");
                    display = new ParticleDisplay_13();
                    hasColorTransition = true;
                    hasColorDataType = true;
                } catch (Throwable not20_5) {
                    try {
                        Particle.valueOf("SHRIEK");
                        display = new ParticleDisplay_13();
                        hasColorTransition = true;
                    } catch (Throwable not19) {
                        try {
                            Particle.valueOf("VIBRATION");
                            display = new ParticleDisplay_13();
                            hasColorTransition = true;
                        } catch (Throwable not17) {
                            try {
                                Particle.valueOf("SQUID_INK");
                                display = new ParticleDisplay_13();
                            } catch (Throwable not13) {
                                display = new ParticleDisplay_12();
                            }
                        }
                    }
                }
            }
        }
        display.setHasColorTransition(hasColorTransition);
        display.setHasColorDataType(hasColorDataType);

        return display;
    }
}
