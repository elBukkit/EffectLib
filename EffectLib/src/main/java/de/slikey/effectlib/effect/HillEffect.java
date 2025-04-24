package de.slikey.effectlib.effect;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.util.VectorUtils;
/**
 * Taken from http://en.wikipedia.org/wiki/Torus
 *
 * @author Kevin
 */
public class HillEffect extends Effect {

    /**
     * Height of the hill in blocks
     */
    public float height = 2.5F;

    /**
     * Amount of particles per row
     */
    public float particles = 30;

    /**
     * Length of the edge
     */
    public float edgeLength = 6.5F;

    /**
     * Rotation of the Hill
     */
    public double yRotation = Math.PI / 7;

    public HillEffect(EffectManager effectManager) {
        super(effectManager);
        type = EffectType.REPEATING;
        particle = Particle.FLAME;
        period = 10;
        iterations = 20;
    }

    @Override
    public void onRun() {
        Location location = getLocation();
        Vector v = new Vector();

        if (location == null) {
            cancel();
            return;
        }

        double y1;
        double y2;

        for (int x = 0; x <= particles; x++) {
            y1 = Math.sin(Math.PI * x / particles);
            for (int z = 0; z <= particles; z++) {
                y2 = Math.sin(Math.PI * z / particles);
                v.setX(edgeLength * x / particles).setZ(edgeLength * z / particles);
                v.setY(height * y1 * y2);
                VectorUtils.rotateAroundAxisY(v, yRotation);

                display(particle, location.add(v));
                location.subtract(v);
            }
        }
    }

}
