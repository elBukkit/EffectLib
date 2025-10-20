package de.slikey.effectlib.util;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ParticleDisplay {

    protected ParticleEffectManager manager;

    protected boolean hasColorTransition = false;
    protected boolean hasColorDataType = false;

    protected static Particle SPELL_MOB;
    protected static Particle SPELL_MOB_AMBIENT;
    protected static Particle ITEM_CRACK;
    protected static Particle BLOCK_CRACK;
    protected static Particle BLOCK_DUST;
    protected static Particle FALLING_DUST;
    protected static Particle REDSTONE;
    protected static Particle DUST_COLOR_TRANSITION;
    protected static Particle VIBRATION;
    protected static Particle SHRIEK;
    protected static Particle SCULK_CHARGE;

    protected static void initializeConstants() {
        if (SPELL_MOB != null) return;
        SPELL_MOB = ParticleUtil.getParticle("SPELL_MOB");
        SPELL_MOB_AMBIENT = ParticleUtil.getParticle("SPELL_MOB_AMBIENT");
        ITEM_CRACK = ParticleUtil.getParticle("ITEM_CRACK");
        BLOCK_CRACK = ParticleUtil.getParticle("BLOCK_CRACK");
        BLOCK_DUST = ParticleUtil.getParticle("BLOCK_DUST");
        FALLING_DUST = ParticleUtil.getParticle("FALLING_DUST");
        REDSTONE = ParticleUtil.getParticle("REDSTONE");
        DUST_COLOR_TRANSITION = ParticleUtil.getParticle("DUST_COLOR_TRANSITION");
        VIBRATION = ParticleUtil.getParticle("VIBRATION");
        SHRIEK = ParticleUtil.getParticle("SHRIEK");
        SCULK_CHARGE = ParticleUtil.getParticle("SCULK_CHARGE");
    }

    public void setHasColorTransition(boolean hasColorTransition) {
        this.hasColorTransition = hasColorTransition;
    }

    public void setHasColorDataType(boolean hasColorDataType) {
        this.hasColorDataType = hasColorDataType;
    }

    public void display(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        initializeConstants();

        // Legacy colorizeable particles
        if (options.color != null && particle == SPELL_MOB) {
            displayLegacyColored(particle, options, center, range, targetPlayers);
            return;
        }

        spawnParticle(particle, options, center, range, targetPlayers);
    }

    protected void spawnParticle(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        try {
            if (targetPlayers == null) {
                double squared = range * range;
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    if (!manager.isVisiblePlayer(player, center, squared)) continue;

                    if (hasColorDataType && particle == SPELL_MOB) {
                        player.spawnParticle(particle, center, options.amount, options.offsetX, options.offsetY, options.offsetZ, options.speed, options.color == null ? Color.WHITE : options.color);
                    } else {
                        player.spawnParticle(particle, center, options.amount, options.offsetX, options.offsetY, options.offsetZ, options.speed, options.data);
                    }

                    displayFakeBlock(player, center, options);
                }
                return;
            }

            for (final Player player : targetPlayers) {
                if (manager.isPlayerIgnored(player)) continue;
                player.spawnParticle(particle, center, options.amount, options.offsetX, options.offsetY, options.offsetZ, options.speed, options.data);
                displayFakeBlock(player, center, options);
            }

        } catch (Exception ex) {
            if (manager != null) manager.onError(ex);
        }
    }

    protected void displayFakeBlock(final Player player, Location center, ParticleOptions options) {
        // Implemented in 1.13+
    }

    @SuppressWarnings({"deprecation"})
    protected void displayItem(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        Material material = options.material;
        if (material == null || material == Material.AIR) return;

        ItemStack item = new ItemStack(material);
        item.setDurability(options.materialData);
        options.data = item;
        spawnParticle(particle, options, center, range, targetPlayers);
    }

    protected void displayLegacyColored(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        // Colored particles can't have a speed of 0.
        Color color = options.color;
        if (color == null) color = Color.RED;
        if (options.speed == 0) options.speed = 1;
        // Amount = 0 is a special flag that means use the offset as color
        options.amount = 0;

        float offsetX = (float) color.getRed() / 255;
        float offsetY = (float) color.getGreen() / 255;
        float offsetZ = (float) color.getBlue() / 255;

        // The redstone particle reverts to red if R is 0!
        if (offsetX < Float.MIN_NORMAL) offsetX = Float.MIN_NORMAL;

        options.offsetX = offsetX;
        options.offsetY = offsetY;
        options.offsetZ = offsetZ;

        spawnParticle(particle, options, center, range, targetPlayers);
    }

    public void setManager(ParticleEffectManager manager) {
        this.manager = manager;
    }

    public boolean hasColorTransition() {
        return hasColorTransition;
    }

}
