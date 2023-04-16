package csuci.seanhulse.fitness.workouts;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.google.mlkit.vision.pose.Pose;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import csuci.seanhulse.fitness.MainActivity;
import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.api.IApiListener;
import csuci.seanhulse.fitness.api.MachineLearningApiHandler;
import csuci.seanhulse.fitness.camera.CameraManager;
import csuci.seanhulse.fitness.data.IPoseDataListener;
import csuci.seanhulse.fitness.data.PoseDataManager;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.skeleton.Skeleton;

/**
 * A fragment for conducting a workout. This fragment is responsible for receiving pose data from a
 * {@link PoseDataManager}, rendering the detected pose onto a {@link Skeleton}, and sending the pose data to a machine
 * learning model for prediction
 * <p>
 * via a {@link MachineLearningApiHandler}.
 */
public class WorkoutFragment extends Fragment implements IApiListener, IPoseDataListener {
    /**
     * Atomic boolean indicating whether the fragment is currently processing a prediction request.
     */
    private static final AtomicBoolean IS_CURRENTLY_PREDICTING = new AtomicBoolean(false);

    /**
     * The maximum number of poses to buffer before processing a prediction request.
     */
    public static final int BUFFER_PROCESS_LIMIT = 20;

    /**
     * The maximum number of poses to buffer before processing a prediction request.
     */
    private final Collection<Pose> poseBuffer = new CopyOnWriteArraySet<>();

    /**
     * The exercise for this workout.
     */
    private final Exercise exercise;

    /**
     * The {@link PoseDataManager} responsible for providing pose data for the workout.
     */
    private PoseDataManager poseDataManager;

    /**
     * The {@link Skeleton} responsible for rendering detected poses.
     */
    private Skeleton skeleton;

    /**
     * The {@link MachineLearningApiHandler} responsible for sending pose data to a machine learning model for
     * prediction.
     */

    private MachineLearningApiHandler machineLearningApiHandler;

    /**
     * The context of this fragment.
     */
    private Context context;

    /**
     * The camera preview view.
     */
    private PreviewView cameraSurface;

    /**
     * Constructs a new {@link WorkoutFragment} with the given exercise.
     *
     * @param exercise The exercise for this workout.
     */
    public WorkoutFragment(Exercise exercise) {
        this.exercise = exercise;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Called when the fragment is creating its view.
     *
     * @param inflater           The layout inflater used to inflate the fragment layout.
     * @param container          The parent view group.
     * @param savedInstanceState The saved instance state of the fragment.
     * @return The inflated view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workout, container, false);
        this.context = view.getContext();

        machineLearningApiHandler = new MachineLearningApiHandler(context);
        machineLearningApiHandler.addListener(this);

        MainActivity activity = (MainActivity) context;

        this.poseDataManager = activity.getPoseDataManager();
        this.skeleton = view.findViewById(R.id.skeleton);
        this.cameraSurface = view.findViewById(R.id.cameraSurface);

        poseDataManager.addPoseDataListener(skeleton);

        // Load the ML Model into our API for processing predictions
        machineLearningApiHandler.httpPostLoadMLModel(exercise);

        return view;
    }

    /**
     * Called when the fragment is being destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        poseDataManager.removePoseDataListener(skeleton);
        machineLearningApiHandler.removeListener(this);
    }

    /**
     * Loads the given fragment into the parent fragment manager.
     *
     * @param fragment The fragment to load.
     */
    private void loadFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.relativeLayout, fragment)
                .addToBackStack(fragment.getTag())
                .commit();
    }

    /**
     * Called when a task creation response is received from the {@link MachineLearningApiHandler}.
     *
     * @param jsonObject The JSON object representing the response.
     */
    @Override
    public void handleTaskCreationResponse(JsonObject jsonObject) {
        JsonObject task = jsonObject.getAsJsonObject("task");

        if (task != null) {
            String id = task.get("id").getAsString();
            machineLearningApiHandler.httpGetTaskResults(id);
        }
    }

    /**
     * This method handles the response from the server after sending a task to the server and performs the necessary
     * actions based on the status of the task.
     *
     * @param jsonObject the JSON response from the server
     */
    @Override
    public void handleTaskResultsResponse(JsonObject jsonObject) {
        Log.d(WorkoutFragment.class.getName(), jsonObject.toString());

        JsonObject task = jsonObject.getAsJsonObject("task");
        String taskStatus = task.get("status").getAsString();
        String taskType = task.get("type").getAsString();

        // If the task is pending, just keep checking until it succeeds or fails
        if (taskStatus.equals("PENDING")) {
            String id = task.get("id").getAsString();
            machineLearningApiHandler.httpGetTaskResults(id);
        } else if (taskStatus.equals("SUCCESS")) {
            switch (taskType) {
                case "MODEL_LOAD":
                    handleModelLoadResponse();
                    break;
                case "MODEL_PREDICT":
                    // TODO: HANDLE MODEL PREDICTION BY DISPLAYING IT ON THE FRAGMENT

                    // We are not predicting anymore because we are handling a response
                    IS_CURRENTLY_PREDICTING.getAndSet(false);

                    break;
            }
        }
    }


    /**
     * Handles the response after successfully loading the model. This method adds a listener to the PoseDataManager and
     * starts the camera. It also sets the IS_CURRENTLY_PREDICTING flag to false since we are not predicting after
     * initially loading the model.
     */
    private void handleModelLoadResponse() {
        poseDataManager.addPoseDataListener(this);

        MainActivity activity = (MainActivity) context;
        CameraManager cameraManager = activity.getCameraManager();
        cameraManager.start(cameraSurface);

        IS_CURRENTLY_PREDICTING.getAndSet(false);
    }

    /**
     * This method is called when a new Pose is added to the pose data manager's list of poses. It provides the added
     * pose as an argument to the method for further processing.
     *
     * @param pose The Pose object that was added to the pose data manager.
     */
    @Override
    public void poseAdded(Pose pose) {
        // Buffer the pose we've analyzed
        poseBuffer.add(pose);

        // If the buffer is large enough, process the buffer
        if (poseBuffer.size() >= BUFFER_PROCESS_LIMIT) {
            if (!IS_CURRENTLY_PREDICTING.getAndSet(true)) {
                // Predict the provided poses
                machineLearningApiHandler.httpPostPredict(exercise, poseBuffer);
                poseBuffer.clear();
            }
        }
    }

    @Override
    public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped) {
        // Ignore
    }
}