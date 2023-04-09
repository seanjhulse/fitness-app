package csuci.seanhulse.fitness.api;

import static csuci.seanhulse.fitness.api.TaskState.Status.FAILURE;
import static csuci.seanhulse.fitness.api.TaskState.Status.PENDING;
import static csuci.seanhulse.fitness.api.TaskState.Status.SUCCESS;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import csuci.seanhulse.fitness.db.Exercise;


/**
 * Handler for making requests to the Machine Learning API.
 *
 * @since 0.1.0
 */
public class MachineLearningApiHandler {
    private static final String bucket = "poses105121-dev";
    private static final String baseUrl = "http://10.38.1.202:5000";
    private static final long BACKOFF_TIMEOUT_MS = 2_000;
    private static final int BACKOFF_RETRIES = 20;
    private static final Gson gson = new Gson();
    private final RequestQueue queue;
    private final Collection<IApiListener> listeners = new ArrayList<>();

    public MachineLearningApiHandler(Context context) {
        this.queue = Volley.newRequestQueue(context);
    }

    public void addListener(IApiListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IApiListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Simple test request to hit the root webpage.
     */
    public void httpGetHome() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, baseUrl,
                (Response.Listener<String>) response -> {
                    Log.i(MachineLearningApiHandler.class.getName(), response);
                },
                (Response.ErrorListener) error -> {
                    Log.e(MachineLearningApiHandler.class.getName(), error.getMessage());
                });

        queue.add(stringRequest);
    }

    /**
     * Request to build the ML model for a given exercise.
     *
     * @param exercise the {@link Exercise} we are building an ML model for
     */
    public void httpPostBuildModel(Exercise exercise) {
        String body = convertExerciseToJsonBodyMlModel(exercise);
        String url = buildUrl("model/build");

        Request<JsonObject> buildModelRequest = new JsonRequest<>(Request.Method.POST, url, body,
                (Response.Listener<JsonObject>) response -> {
                    listeners.forEach(listener -> listener.handleTaskCreationResponse(response));
                },
                (Response.ErrorListener) error -> {
                    error.printStackTrace();
                    Log.e(MachineLearningApiHandler.class.getName(), "Failed to build the ML model");
                }) {
            @Override
            protected Response<JsonObject> parseNetworkResponse(NetworkResponse response) {
                if (response.statusCode == HttpsURLConnection.HTTP_OK) {
                    try {
                        Log.i(MachineLearningApiHandler.class.getName(), response.toString());
                        String data = new String(response.data);
                        JsonObject json = gson.fromJson(data, JsonObject.class);
                        return Response.success(json, null);
                    } catch (Exception e) {
                        Log.e("Failed to parse JSON", e.getMessage());
                    }
                }
                return Response.error(new VolleyError(response));
            }
        };

        queue.add(buildModelRequest);
    }

    public void httpGetTaskResults(String taskId) {
        httpGetTaskResultsBackoff(taskId, BACKOFF_RETRIES);
    }

    public void httpGetTaskResultsBackoff(String taskId, int numRetries) {

        if (numRetries <= 0) {
            Log.w(MachineLearningApiHandler.class.getName(),
                    String.format("We've hit our backoff limit for %s", taskId));
            return;
        }

        final int remainingRetries = numRetries - 1;

        String url = buildUrl(String.format("tasks/%s/result", taskId));

        Request<JsonObject> getTaskResults = new JsonRequest<>(Request.Method.GET, url, null,
                (Response.Listener<JsonObject>) response -> {

                    JsonObject task = response.getAsJsonObject("task");
                    TaskState.Status status = TaskState.Status.valueOf(task.get("status").getAsString());

                    if (status.equals(SUCCESS) || status.equals(FAILURE)) {

                        listeners.forEach(listener -> listener.handleTaskResultsResponse(response));

                    } else if (status.equals(PENDING)) {

                        Log.i(MachineLearningApiHandler.class.getName(), String.format("Retrying request: %s", taskId));

                        // Every retry, we increase the backoff time
                        long backOffTimeoutMs = 0L;
                        if (numRetries > 1) {
                            backOffTimeoutMs = BACKOFF_TIMEOUT_MS * (BACKOFF_RETRIES / numRetries);
                        }

                        // Create a timer and schedule a retry of the network request
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                httpGetTaskResultsBackoff(taskId, remainingRetries);
                            }
                        }, backOffTimeoutMs);
                    }

                },
                (Response.ErrorListener) error -> {
                    error.printStackTrace();
                    Log.e(MachineLearningApiHandler.class.getName(),
                            String.format("Request for task results failed: %s",
                                    taskId));
                }) {
            @Override
            public Response<JsonObject> parseNetworkResponse(NetworkResponse response) {
                if (response.statusCode == HttpsURLConnection.HTTP_OK) {
                    try {
                        Log.i(MachineLearningApiHandler.class.getName(), response.toString());
                        String data = new String(response.data);
                        JsonObject json = gson.fromJson(data, JsonObject.class);
                        return Response.success(json, null);
                    } catch (Exception e) {
                        Log.e("Failed to parse JSON", e.getMessage());
                    }
                }
                return Response.error(new VolleyError(response));
            }
        };

        queue.add(getTaskResults);

    }

    /**
     * Given a path, builds the URL from the base url.
     *
     * @param path the path in the API
     * @return a fully qualified URL
     */
    private String buildUrl(String path) {
        return String.format("%s/%s", baseUrl, path);
    }

    /**
     * Converts a given {@link Exercise} into a valid, stringified, JSON body.
     * <p>
     * Example request: { "bucket": "poses105121-dev", "prefix": "public/squats deque-3", "name": "squats-deque-3" }
     *
     * @return a String of JSON for posting to our ML endpoints
     */
    private String convertExerciseToJsonBodyMlModel(Exercise exercise) {

        String name = exercise.getName();

        String prefix = String.format("public/%s-%s", name.toLowerCase(), exercise.getId());
        JsonObject exerciseJson = new JsonObject();
        exerciseJson.addProperty("bucket", bucket);
        exerciseJson.addProperty("prefix", prefix);
        exerciseJson.addProperty("name", String.format("%s-%s", name.toLowerCase(), exercise.getId()));

        Log.i(MachineLearningApiHandler.class.getName(), String.format("Building model for %s: %s", bucket, prefix));

        return exerciseJson.toString();
    }
}
