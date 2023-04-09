package csuci.seanhulse.fitness.api;

/**
 * Manages the state of a "Task" which is a concept of our API. We are using Celery in Flask to manage background tasks
 * for building ML models or running predictions. We need to request results of a task in order to know if they
 * succeeded or failed.
 *
 * @since 0.1.0
 */
public class TaskState {
    public enum Status {
        SUCCESS,
        // Pending is a unique state (non-existing IDs will result in PENDING)
        PENDING,
        FAILURE
    }

    private final String id;

    public TaskState(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
