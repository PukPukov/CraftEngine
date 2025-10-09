package ru.mrbedrockpy.craftengine.server.util;

import java.util.function.Consumer;
import java.util.function.DoublePredicate;

public class Util {
    
    // GENERIC LERP
    
    /**
     * @param in millis
     */
    public static double genericLerpStep(double lower, double higher, double in) {
        return (higher - lower) / (in * 1_000_000);
    }
    
    /**
     * @param step obtained via genericLerpStep(double lower, double higher, double in)
     */
    public static void genericLerp(
        long previousTimeNanos, long currentTimeNanos,
        double actual, double target, Consumer<Double> actualSetter,
        double step,
        boolean phase, Consumer<Boolean> phaseSetter
    ) {
        if (phase) {
            long timeBetweenCalls = currentTimeNanos - previousTimeNanos;
            double madeFromLastCall = timeBetweenCalls*step;
            boolean direction = actual < target;
            if (direction) oneWayGenericLerp(
                actual-madeFromLastCall,
                (next) -> next < target,
                actualSetter, phaseSetter,
                target
            ); else oneWayGenericLerp(
                actual+madeFromLastCall,
                (next) -> next > target,
                actualSetter, phaseSetter,
                target
            );
        }
    }
    
    private static void oneWayGenericLerp(
        double next,
        DoublePredicate done,
        Consumer<Double> actualSetter, Consumer<Boolean> phaseSetter,
        double limit
    ) {
        if (!done.test(next)) {
            actualSetter.accept(limit);
            phaseSetter.accept(false);
        } else actualSetter.accept(next);
    }
    
    // GENERIC LERP.END
    
}