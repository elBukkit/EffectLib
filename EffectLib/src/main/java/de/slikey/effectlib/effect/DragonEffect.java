package de.slikey.effectlib.effect;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.util.MathUtils;
import de.slikey.effectlib.util.RandomUtils;
import de.slikey.effectlib.util.VectorUtils;

public class DragonEffect extends Effect {

    protected final List<Float> rndF;
    protected final List<Double> rndAngle;

    /**
     * Pitch of the dragon arc
     */
    public float pitch = 0.1F;

    /**
     * Arcs to build the breath
     */
    public int arcs = 20;

    /**
     * Particles per arc
     */
    public int particles = 30;

    /**
     * Steps per iteration
     */
    public int stepsPerIteration = 2;

    /**
     * Length in blocks
     */
    public float length = 4;

    /**
     * Current step. Works as counter
     */
    protected int step = 0;

    public DragonEffect(EffectManager effectManager) {
        super(effectManager);
        type = EffectType.REPEATING;
        particle = Particle.FLAME;
        period = 2;
        iterations = 200;
        rndF = new ArrayList<>(arcs);
        rndAngle = new ArrayList<>(arcs);
    }

    @Override
    public void reset() {
        step = 0;
    }

    @Override
    public void onRun() {
        Location location = getLocation();

        if (location == null) {
            cancel();
            return;
        }

        float pitch;
        float x;
        float y;
        Vector v;

        for (int j = 0; j < stepsPerIteration; j++) {
            if (step % particles == 0) {
                rndF.clear();
                rndAngle.clear();
            }

            while (rndF.size() < arcs) {
                rndF.add(RandomUtils.random.nextFloat());
            }

            while (rndAngle.size() < arcs) {
                rndAngle.add(RandomUtils.getRandomAngle());
            }

            for (int i = 0; i < arcs; i++) {
                pitch = rndF.get(i) * 2 * this.pitch - this.pitch;
                x = (step % particles) * length / particles;
                y = (float) (pitch * Math.pow(x, 2));
                v = new Vector(x, y, 0);
                VectorUtils.rotateAroundAxisX(v, rndAngle.get(i));
                VectorUtils.rotateAroundAxisZ(v, -location.getPitch() * MathUtils.degreesToRadians);
                VectorUtils.rotateAroundAxisY(v, -(location.getYaw() + 90) * MathUtils.degreesToRadians);
                display(particle, location.add(v));
                location.subtract(v);
            }
            step++;
        }
    }

}
