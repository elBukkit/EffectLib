package de.slikey.effectlib.util.versions;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
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
        if (!addParticleData(particle, options, center)) {
            return;
        }
        spawnParticle(particle, options, center, range, targetPlayers);
    }

    public boolean addParticleData(Particle particle, ParticleOptions options, Location center) {
        Class<?> dataType = particle.getDataType();
        if (dataType != null && dataType.isAssignableFrom(ItemStack.class)) {
            Material material = options.material;
            if (material == null || material == Material.AIR) {
                return false;
            }
            ItemStack item = new ItemStack(material);
            item.setDurability(options.materialData);
            options.data = item;
            return true;
        }

        if (dataType != null && dataType.isAssignableFrom(BlockData.class)) {
            Material material = options.material;
            if (material == null || material.name().contains("AIR")) {
                return false;
            }
            try {
                options.data = material.createBlockData();
            } catch (Exception ex) {
                manager.onError("Error creating block data for " + material, ex);
            }
            return options.data != null;
        }

        if (dataType != null && dataType.isAssignableFrom(Particle.DustOptions.class)) {
            // color is required
            if (options.color == null) options.color = Color.RED;
            options.data = new Particle.DustOptions(options.color, options.size);
            return true;
        }

        if (dataType != null && dataType.isAssignableFrom(Particle.DustTransition.class)) {
            if (options.color == null) options.color = Color.RED;
            if (options.toColor == null) options.toColor = options.color;
            options.data = new Particle.DustTransition(options.color, options.toColor, options.size);
            return true;
        }

        if (dataType != null && dataType.isAssignableFrom(Color.class)) {
            if (options.color == null) options.color = Color.RED;
            options.data = options.color;
            return true;
        }

        if (dataType != null && dataType.isAssignableFrom(Vibration.class)) {
            if (options.target == null) return false;

            Vibration.Destination destination;
            Entity targetEntity = options.target.getEntity();
            if (targetEntity != null) destination = new Vibration.Destination.EntityDestination(targetEntity);
            else {
                Location targetLocation = options.target.getLocation();
                if (targetLocation == null) return false;

                destination = new Vibration.Destination.BlockDestination(targetLocation);
            }

            options.data = new Vibration(center, destination, options.arrivalTime);
            return true;
        }

        // These two can't use datatype because they are just ints and floats
        if (particle == SHRIEK) {
            if (options.shriekDelay < 0) options.shriekDelay = 0;
            options.data = options.shriekDelay;
            return true;
        }

        if (particle == SCULK_CHARGE) {
            options.data = options.sculkChargeRotation;
            return true;
        }

        return true;
    }
}
