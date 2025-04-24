package de.slikey.effectlib.util.versions;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.slikey.effectlib.util.ParticleDisplay;
import de.slikey.effectlib.util.ParticleOptions;

public class ParticleDisplay_Modern extends ParticleDisplay {

    public void display(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        initializeConstants();

        // Legacy colorizeable particles
        // 1.20.5 has removed Particle#SPELL_MOB_AMBIENT and SPELL_MOB is now ENTITY_EFFECT (handled by ParticleUtil)
        if (options.color != null && particle == SPELL_MOB) {
            displayLegacyColored(particle, options, center, range, targetPlayers);
            return;
        }

        if (particle == ITEM_CRACK) {
            displayItem(particle, options, center, range, targetPlayers);
            return;
        }

        if (particle == BLOCK_DUST || particle == FALLING_DUST) {
            Material material = options.material;
            if (material == null || material.name().contains("AIR")) return;
            try {
                options.data = material.createBlockData();
            } catch (Exception ex) {
                manager.onError("Error creating block data for " + material, ex);
            }
            if (options.data == null) return;
        }

        if (particle == REDSTONE) {
            // color is required
            if (options.color == null) options.color = Color.RED;
            options.data = new Particle.DustOptions(options.color, options.size);
        }

        if (particle == DUST_COLOR_TRANSITION) {
            if (options.color == null) options.color = Color.RED;
            if (options.toColor == null) options.toColor = options.color;
            options.data = new Particle.DustTransition(options.color, options.toColor, options.size);
        }

        if (particle == VIBRATION) {
            if (options.target == null) return;

            Vibration.Destination destination;
            Entity targetEntity = options.target.getEntity();
            if (targetEntity != null) destination = new Vibration.Destination.EntityDestination(targetEntity);
            else {
                Location targetLocation = options.target.getLocation();
                if (targetLocation == null) return;

                destination = new Vibration.Destination.BlockDestination(targetLocation);
            }

            options.data = new Vibration(center, destination, options.arrivalTime);
        }

        if (particle == SHRIEK) {
            if (options.shriekDelay < 0) options.shriekDelay = 0;
            options.data = options.shriekDelay;
        }

        if (particle == SCULK_CHARGE) {
            options.data = options.sculkChargeRotation;
        }

        spawnParticle(particle, options, center, range, targetPlayers);
    }

    @SuppressWarnings({"deprecation"})
    protected void displayItem(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        Material material = options.material;
        if (material == null || material.isAir()) return;

        ItemStack item = new ItemStack(material);
        item.setDurability(options.materialData);
        options.data = item;
        spawnParticle(particle, options, center, range, targetPlayers);
    }
}
