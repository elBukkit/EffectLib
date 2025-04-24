package de.slikey.effectlib.math;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

public class EquationStore {

    private static final String DEFAULT_VARIABLE = "x";
    private static EquationStore instance;
    private final Map<String, EquationTransform> transforms = new HashMap<>();

    public EquationTransform getTransform(String equation) {
        return getTransform(equation, DEFAULT_VARIABLE);
    }

    public EquationTransform getTransform(String equation, String variable) {
        EquationTransform transform = transforms.get(equation);
        if (transform == null) {
            transform = new EquationTransform(equation, variable);
            transforms.put(equation, transform);
        }

        return transform;
    }

    public EquationTransform getTransform(String equation, String... variables) {
        String equationKey = equation + ":" + String.join(",", variables);
        EquationTransform transform = transforms.get(equationKey);
        if (transform == null) {
            transform = new EquationTransform(equation, variables);
            transforms.put(equationKey, transform);
        }

        return transform;
    }

    public EquationTransform getTransform(String equation, Collection<String> variables) {
        String equationKey = equation + ":" + String.join(",", variables);
        EquationTransform transform = transforms.get(equationKey);
        if (transform == null) {
            transform = new EquationTransform(equation, variables);
            transforms.put(equationKey, transform);
        }
        
        return transform;
    }

    public static void clear() {
        if (instance != null) instance.transforms.clear();
    }
    
    public static EquationStore getInstance() {
        if (instance == null) instance = new EquationStore();
        
        return instance;
    }

}
