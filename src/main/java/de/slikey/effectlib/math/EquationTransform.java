package de.slikey.effectlib.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.VariableProvider;
import net.objecthunter.exp4j.function.Function;

public class EquationTransform implements Transform, VariableProvider {

    private Expression expression;
    private static Function randFunction;
    private static Function probabilityFunction;
    private static Function minFunction;
    private static Function maxFunction;
    private static Function selectFunction;
    private final Collection<String> inputVariables;
    private EquationVariableProvider variableProvider;
    private Exception exception;

    @Override
    public void load(ConfigurationSection parameters) {
        setEquation(parameters.getString("equation", ""));
    }

    public EquationTransform() {
        inputVariables = new ArrayList<>();
    }
    
    public EquationTransform(String equation) {
        this(equation, "t");
    }

    public EquationTransform(String equation, String inputVariable) {
        inputVariables = new ArrayList<>();
        inputVariables.add(inputVariable);
        setEquation(equation);
    }

    public EquationTransform(String equation, String... inputVariables) {
        this.inputVariables = new ArrayList<>();
        for (String inputVariable : inputVariables) {
            this.inputVariables.add(inputVariable);
        }
        setEquation(equation);
    }

    public EquationTransform(String equation, Collection<String> inputVariables) {
        this.inputVariables = inputVariables;
        setEquation(equation);
    }

    private void checkCustomFunctions() {
        if (randFunction == null) {
            randFunction = new Function("rand", 2) {
                private Random random = new Random();

                @Override
                public double apply(double... args) {
                    return random.nextDouble() * (args[1] - args[0]) + args[0];
                }
            };
        }
        if (probabilityFunction == null) {
            probabilityFunction = new Function("prob", 3) {
                private Random random = new Random();

                @Override
                public double apply(double... args) {
                    return random.nextDouble() < args[0] ? args[1] : args[2];
                }
            };
        }
        if (minFunction == null) {
            minFunction = new Function("min", 2) {
                @Override
                public double apply(double... args) {
                    return Math.min(args[0], args[1]);
                }
            };
        }
        if (maxFunction == null) {
            maxFunction = new Function("max", 2) {
                @Override
                public double apply(double... args) {
                    return Math.max(args[0], args[1]);
                }
            };
        }
        if (selectFunction == null) {
            selectFunction = new Function("select", 4) {
                @Override
                public double apply(double... args) {
                    if (args[0] < 0) return args[1];
                    else if (args[0] == 0) return args[2];
                    return args[3];
                }
            };
        }
    }

    public boolean setEquation(String equation) {
        try {
            checkCustomFunctions();
            exception = null;
            expression = new ExpressionBuilder(equation)
                .function(randFunction)
                .function(probabilityFunction)
                .function(minFunction)
                .function(maxFunction)
                .function(selectFunction)
                .variables(new HashSet<String>(inputVariables))
                .build();
            expression.setVariableProvider(this);
        } catch (Exception ex) {
            expression = null;
            exception = ex;
        }
        
        return exception == null;
    }

    @Override
    public synchronized double get(double t) {
        if (expression == null) return 0;

        for (String inputVariable : inputVariables) {
            expression.setVariable(inputVariable, t);
        }
        return get();
    }
    
    public synchronized double get(double... t) {
        if (expression == null) return 0;

        int index = 0;
        for (String inputVariable : inputVariables) {
            expression.setVariable(inputVariable, t[index]);
            if (index < t.length - 1) index++;
        }
        return get();
    }
    
    public void addVariable(String key) {
        inputVariables.add(key);
    }

    public void setVariable(String key, double value) {
        if (expression != null) expression.setVariable(key, value);
    }

    // Note that this call is *not* synchronized, synchronization here would be up to the caller
    // and would need to be done inside a block that also includes the setVariable calls.
    // EffectLib does not use this method as of this time.
    public double get() {
        if (expression == null) return Double.NaN;

        double value = Double.NaN;
        try {
            exception = null;
            value = expression.evaluate();
        } catch (Exception ex) {
            exception = ex;
        }
        return value;
    }
    
    public Exception getException() {
        return exception;
    }

    public boolean isValid() {
        return exception == null;
    }
    
    public Collection<String> getParameters() {
        return inputVariables;
    }

    public void setVariableProvider(EquationVariableProvider provider) {
        variableProvider = provider;
    }

    @Override
    public Double getVariable(String variable) {
        return variableProvider == null ? null : variableProvider.getVariable(variable);
    }
}
