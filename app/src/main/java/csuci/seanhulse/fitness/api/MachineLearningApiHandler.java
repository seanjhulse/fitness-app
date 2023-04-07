package csuci.seanhulse.fitness.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


/**
 * Handler for making requests to the Machine Learning API.
 *
 * @since 0.1.0
 */
public class MachineLearningApiHandler {
    private static final String baseUrl = "http://10.38.1.202:5000";
    private final RequestQueue queue;

    public MachineLearningApiHandler(Context context) {
        this.queue = Volley.newRequestQueue(context);
    }

    public void httpGetHome(Context context) {
        Log.i("MachineLearningApiHandler", "Hello world");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, baseUrl,
                (Response.Listener<String>) response -> {
                    // TODO: Handle response
                    Log.i("MachineLearningApiHandler", response);
                },
                (Response.ErrorListener) error -> {
                    // TODO: Handle error
                    Log.e("MachineLearningApiHandler", error.getMessage());
                });

        queue.add(stringRequest);
    }
}
