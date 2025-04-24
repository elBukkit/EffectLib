package de.slikey.effectlib.math;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

public class MultiplyTransform implements Transform {

    private Collection<Transform> inputs;

    @Override
    public void load(ConfigurationSection parameters) {
        inputs = Transforms.loadTransformList(parameters, "inputs");
    }

    @Override
    public double get(double input) {
        double value = 1;
        for (Transform transform : inputs) {
            value *= transform.get(input);
        }
        return value;
    }

}
