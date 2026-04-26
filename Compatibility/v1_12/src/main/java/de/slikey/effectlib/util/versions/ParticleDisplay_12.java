package de.slikey.effectlib.util.versions;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import de.slikey.effectlib.util.ParticleDisplay;
import de.slikey.effectlib.util.ParticleOptions;

public class ParticleDisplay_12 extends ParticleDisplay {
    public void display(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        initializeConstants();

        // Legacy colorizeable particles
        if (options.color != null && particle == SPELL_MOB) {
            displayLegacyColored(particle, options, center, range, targetPlayers);
            return;
        }

        super.display(particle, options, center, range, targetPlayers);
    }
}
