package de.slikey.effectlib.util.versions;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import de.slikey.effectlib.util.ParticleDisplay;
import de.slikey.effectlib.util.ParticleOptions;

public class ParticleDisplay_13 extends ParticleDisplay {
    public void display(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
        initializeConstants();

        // Legacy colorizeable particles
        if (options.color != null && particle == SPELL_MOB) {
            displayLegacyColored(particle, options, center, range, targetPlayers);
            return;
        }

        super.display(particle, options, center, range, targetPlayers);
    }

    protected void displayFakeBlock(final Player player, Location center, ParticleOptions options) {
        if (options.blockData == null) return;
        if (!center.getBlock().isEmpty()) return;

        BlockData blockData = Bukkit.createBlockData(options.blockData.toLowerCase());
        final Location b = center.getBlock().getLocation().clone();
        player.sendBlockChange(b, blockData);

        Bukkit.getScheduler().runTaskLaterAsynchronously(manager.getOwningPlugin(), new Runnable() {
            @Override
            public void run() {
                player.sendBlockChange(b, b.getBlock().getBlockData());
            }
        }, options.blockDuration);
    }
}
