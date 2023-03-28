package csuci.seanhulse.fitness.db;

/**
 * Represents the state of this exercise's training. This is an enum to help differentiate how effective the ML model is
 * likely to be. For example, a high accuracy model will be a "high level" and a low accuracy model will be a "low
 * level". Accuracy is likely to be based on the amount of training data for any given model.
 *
 * @since 0.1.0
 */
public enum Level {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH");

    private final String name;

    Level(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
