package de.slikey.effectlib.effect;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.util.MathUtils;
import de.slikey.effectlib.util.VectorUtils;

public class VortexEffect extends Effect {

    /**
     * Radius of vortex (2)
     */
    public float radius = 2;

    /**
     * Radius grow per iteration (0.00)
     */
    public float radiusGrow = 0.00F;

    /**
     * Initial range of the vortex (0.0)
     */
    public float initRange = 0.0F;

    /**
     * Growing per iteration (0.05)
     */
    public float grow = 0.05F;

    /**
     * Radials per iteration (PI / 16)
     */
    public double radials = Math.PI / 16;

    /**
     * Helix-circles per iteration (3)
     */
    public int circles = 3;

    /**
     * Amount of helices (4)
     * Yay for the typo
     */
    public int helixes = 4;

    /**
     * Current step. Works as counter
     */
    protected int step = 0;

    public VortexEffect(EffectManager effectManager) {
        super(effectManager);
        type = EffectType.REPEATING;
        particle = Particle.FLAME;
        period = 1;
        iterations = 200;
    }

    @Override
    public void reset() {
        step = 0;
    }

    @Override
    public void onRun() {
        Location location = getLocation();
        double angle;
        Vector v;

        if (location == null) {
            cancel();
            return;
        }

        for (int x = 0; x < circles; x++) {
            for (int i = 0; i < helixes; i++) {
                angle = step * radials + (2 * Math.PI * i / helixes);
                v = new Vector(Math.cos(angle) * (radius + step * radiusGrow), initRange + step * grow, Math.sin(angle) * (radius + step * radiusGrow));
                VectorUtils.rotateAroundAxisX(v, (location.getPitch() + 90) * MathUtils.degreesToRadians);
                VectorUtils.rotateAroundAxisY(v, -location.getYaw() * MathUtils.degreesToRadians);

                location.add(v);
                display(particle, location);
                location.subtract(v);
            }
            step++;
        }
    }

}
