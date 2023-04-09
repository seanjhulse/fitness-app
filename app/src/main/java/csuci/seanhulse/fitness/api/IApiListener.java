package csuci.seanhulse.fitness.api;

import com.google.gson.JsonObject;

/**
 * Listeners should handle any responses from our API.
 *
 * @since 0.1.0
 */
public interface IApiListener {
    void handleTaskCreationResponse(JsonObject jsonObject);

    void handleTaskResultsResponse(JsonObject jsonObject);
}
