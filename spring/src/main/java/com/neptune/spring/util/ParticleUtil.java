package com.neptune.spring.util;

import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.Location;

public class ParticleUtil {
    public static void playParticles(Player player, Particle particle) {
        Location location = player.getLocation();
        player.getWorld().spawnParticle(particle, location.add(0, 0.5, 0), 10, 0.5, 0.5, 0.5, 0.1);
    }
    
    public static void playParticles(Player player, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        Location location = player.getLocation();
        player.getWorld().spawnParticle(particle, location.add(0, 0.5, 0), count, offsetX, offsetY, offsetZ, speed);
    }
    
    public static void playCustomParticles(Player player, String particleType, int count) {
        try {
            Particle particle = Particle.valueOf(particleType.toUpperCase().replace("MINECRAFT:", ""));
            playParticles(player, particle, count, 0.5, 0.5, 0.5, 0.1);
        } catch (IllegalArgumentException e) {
            // Fallback to default particle
            playParticles(player, Particle.ITEM_SLIME, count, 0.5, 0.5, 0.5, 0.1);
        }
    }
}
