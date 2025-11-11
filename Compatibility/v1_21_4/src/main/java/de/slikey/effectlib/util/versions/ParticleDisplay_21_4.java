package de.slikey.effectlib.util.versions;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import de.slikey.effectlib.util.ParticleOptions;

public class ParticleDisplay_21_4 extends ParticleDisplay_Modern {

	protected void spawnParticle(Particle particle, ParticleOptions options, Location center, double range, List<Player> targetPlayers) {
		try {
			boolean forceShow = options.forceShow || manager.getForceShow();
			if (targetPlayers == null) {
				double squared = range * range;
				for (final Player player : Bukkit.getOnlinePlayers()) {
					if (!manager.isVisiblePlayer(player, center, squared)) continue;
					player.spawnParticle(particle, center, options.amount, options.offsetX, options.offsetY, options.offsetZ, options.speed, options.data, forceShow);
					displayFakeBlock(player, center, options);
				}
				return;
			}

			for (final Player player : targetPlayers) {
				if (manager.isPlayerIgnored(player)) continue;
				player.spawnParticle(particle, center, options.amount, options.offsetX, options.offsetY, options.offsetZ, options.speed, options.data, forceShow);
				displayFakeBlock(player, center, options);
			}

		} catch (Exception ex) {
			if (manager != null) manager.onError("Particle " + particle.getKeyOrNull() + " data type " + particle.getDataType(), ex);
		}
	}

	protected void displayFakeBlock(final Player player, Location center, ParticleOptions options) {
		if (options.blockData == null) return;
		if (!center.getBlock().isPassable() && !center.getBlock().isEmpty()) return;

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
