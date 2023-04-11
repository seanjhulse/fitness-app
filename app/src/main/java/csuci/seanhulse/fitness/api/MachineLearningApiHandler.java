package csuci.seanhulse.fitness.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.db.Landmark;


/**
 * Handler for making requests to the Machine Learning API.
 *
 * @since 0.1.0
 */
public class MachineLearningApiHandler {
    private static final String BUCKET = "poses105121-dev";
    private static final String BASE_URL = "http://10.38.1.202:5000";
    private static final long BACKOFF_TIMEOUT_MS = 10_000;
    private static final int BACKOFF_RETRIES = 20;
    private static final Gson gson = new Gson();
    private final RequestQueue queue;
    private final Collection<IApiListener> listeners = new ArrayList<>();
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy() {
        @Override
        public int getCurrentTimeout() {
            return 50_000;
        }

        @Override
        public int getCurrentRetryCount() {
            return 50_000;
        }

        @Override
        public void retry(VolleyError error) {

        }
    };

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
        StringRequest stringRequest = new StringRequest(Request.Method.GET, BASE_URL,
                response -> {
                    Log.i(MachineLearningApiHandler.class.getName(), response);
                },
                error -> {
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
                response -> {
                    listeners.forEach(listener -> listener.handleTaskCreationResponse(response));
                },
                error -> {
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

    public void httpPostLoadMLModel(Exercise exercise) {
        String body = convertExerciseToJsonBodyLoadMlModel(exercise);
        String url = buildUrl("model/load");

        Request<JsonObject> loadModelRequest = new JsonRequest<>(Request.Method.POST, url, body,
                response -> {
                    listeners.forEach(listener -> listener.handleTaskCreationResponse(response));
                },
                error -> {
                    error.printStackTrace();
                    Log.e(MachineLearningApiHandler.class.getName(), "Failed to load the ML model");
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

        loadModelRequest.setRetryPolicy(DEFAULT_RETRY_POLICY);

        queue.add(loadModelRequest);
    }

    public void httpPostPredict(Exercise exercise, Pose pose) {
        String body = convertPoseToPredictionFormat(exercise, pose);
        String url = buildUrl("model/predict");

        Request<JsonObject> predictionRequest = new JsonRequest<>(Request.Method.POST, url, body,
                response -> {
                    listeners.forEach(listener -> listener.handleTaskCreationResponse(response));
                },
                error -> {
                    error.printStackTrace();
                    Log.e(MachineLearningApiHandler.class.getName(), "Failed to predict the pose results");
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

        predictionRequest.setRetryPolicy(DEFAULT_RETRY_POLICY);

        queue.add(predictionRequest);
    }

    public void httpGetTaskResults(String taskId) {
        String url = buildUrl(String.format("tasks/%s/result", taskId));

        Request<JsonObject> getTaskResults = new JsonRequest<>(Request.Method.GET, url, null,
                response -> listeners.forEach(listener -> listener.handleTaskResultsResponse(response)),
                error -> {
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

        getTaskResults.setRetryPolicy(DEFAULT_RETRY_POLICY);

        queue.add(getTaskResults);

    }

    /**
     * Given a path, builds the URL from the base url.
     *
     * @param path the path in the API
     * @return a fully qualified URL
     */
    private String buildUrl(String path) {
        return String.format("%s/%s", BASE_URL, path);
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
        String mlModelName = String.format("%s-%s", name.toLowerCase(), exercise.getId());
        JsonObject exerciseJson = new JsonObject();
        exerciseJson.addProperty("bucket", BUCKET);
        exerciseJson.addProperty("prefix", prefix);
        exerciseJson.addProperty("name", mlModelName);

        Log.i(MachineLearningApiHandler.class.getName(), String.format("Building model for %s/%s/%s", BUCKET, prefix,
                mlModelName));

        return exerciseJson.toString();
    }

    private String convertExerciseToJsonBodyLoadMlModel(Exercise exercise) {
        String name = exercise.getName();

        String prefix = String.format("public/%s-%s", name.toLowerCase(), exercise.getId());
        String modelFileName = String.format("%s-%s", name.toLowerCase(), exercise.getId());
        JsonObject mlModelJson = new JsonObject();
        mlModelJson.addProperty("bucket", BUCKET);
        mlModelJson.addProperty("prefix", prefix);
        mlModelJson.addProperty("name", modelFileName);

        Log.i(MachineLearningApiHandler.class.getName(), String.format("Loading model for %s (%s): %s", BUCKET,
                prefix, modelFileName));

        return mlModelJson.toString();
    }

    private String convertPoseToPredictionFormat(Exercise exercise, Pose pose) {
        String name = exercise.getName();

        List<Landmark> landmarks = createDbLandmarkFromPose(pose.getAllPoseLandmarks());

        String prefix = String.format("public/%s-%s", name.toLowerCase(), exercise.getId());

        JsonObject exerciseJson = new JsonObject();
        exerciseJson.addProperty("bucket", BUCKET);
        exerciseJson.addProperty("prefix", prefix);
        exerciseJson.addProperty("name", String.format("%s-%s", name.toLowerCase(), exercise.getId()));

        JsonObject landmarksElement = new JsonObject();
        landmarksElement.add("landmarks", convertLandmarksToJson(landmarks));
        exerciseJson.add("pose", landmarksElement);

        Log.i(MachineLearningApiHandler.class.getName(), String.format("Running prediction for pose for %s/%s/%s",
                BUCKET, prefix, name));

        return exerciseJson.toString();
    }

    private JsonArray convertLandmarksToJson(List<Landmark> landmarks) {
        JsonArray landmarksJson = new JsonArray();
        for (Landmark landmark : landmarks) {
            JsonObject landmarkJson = new JsonObject();
            landmarkJson.addProperty("landmarkType", landmark.getLandmarkType());
            landmarkJson.addProperty("confidence", landmark.getConfidence());
            landmarkJson.addProperty("id", landmark.getId());
            landmarkJson.addProperty("x", landmark.getX());
            landmarkJson.addProperty("y", landmark.getY());
            landmarkJson.addProperty("z", landmark.getZ());

            landmarksJson.add(landmarkJson);
        }

        return landmarksJson;
    }

    private List<Landmark> createDbLandmarkFromPose(List<PoseLandmark> poseLandmarks) {
        return poseLandmarks
                .stream()
                .map(landmark -> {
                    PointF3D position = landmark.getPosition3D();
                    return new Landmark(position.getX(),
                            position.getY(),
                            position.getZ(),
                            landmark.getLandmarkType(),
                            landmark.getInFrameLikelihood());
                })
                .collect(Collectors.toList());
    }
}
