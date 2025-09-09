package com.neptune.spring.bounce;

public class LevelSpec {
    private final String name;
    private final double verticalVelocity;
    private final double horizontalMultiplier;
    private final boolean anglePreservation;

    public LevelSpec(String name, double verticalVelocity, double horizontalMultiplier, boolean anglePreservation) {
        this.name = name;
        this.verticalVelocity = verticalVelocity;
        this.horizontalMultiplier = horizontalMultiplier;
        this.anglePreservation = anglePreservation;
    }
    
    // Methods for physics calculation
}
