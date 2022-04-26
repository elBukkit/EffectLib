package de.slikey.effectlib.util.versions;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import de.slikey.effectlib.util.ParticleDisplay;
import de.slikey.effectlib.util.ParticleOptions;

public class ParticleDisplay_17 extends ParticleDisplay {

    @Override
    public void display(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        // Legacy colorizeable particles
        if (options.color != null && (particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT)) {
            displayLegacyColored(particle, options, center, range, targetPlayers);
            return;
        }

        if (particle == Particle.ITEM_CRACK) {
            displayItem(particle, options, center, range, targetPlayers);
            return;
        }

        if (particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST || particle == Particle.FALLING_DUST) {
            Material material = options.material;
            if (material == null || material.name().contains("AIR")) return;
            try {
                options.data = material.createBlockData();
            } catch (Exception ex) {
                manager.onError("Error creating block data for " + material, ex);
            }
            if (options.data == null) return;
        }

        if (particle == Particle.REDSTONE) {
            // color is required
            if (options.color == null) options.color = Color.RED;
            options.data = new Particle.DustOptions(options.color, options.size);
        }

        if (particle == Particle.DUST_COLOR_TRANSITION) {
            if (options.color == null) options.color = Color.RED;
            if (options.toColor == null) options.toColor = options.color;
            options.data = new Particle.DustTransition(options.color, options.toColor, options.size);
        }

        if (particle == Particle.VIBRATION) {
            if (options.target == null) {
                return;
            }
            Vibration.Destination destination;
            Entity targetEntity = options.target.getEntity();
            if (targetEntity != null) {
                destination = new Vibration.Destination.EntityDestination(targetEntity);
            } else {
                Location targetLocation = options.target.getLocation();
                if (targetLocation == null) {
                    return;
                }
                destination = new Vibration.Destination.BlockDestination(targetLocation);
            }

            options.data = new Vibration(center, destination, options.arrivalTime);
        }

        spawnParticle(particle, options, center, range, targetPlayers);
    }
}
