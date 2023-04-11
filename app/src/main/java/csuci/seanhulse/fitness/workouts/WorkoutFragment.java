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
 * A simple {@link Fragment} subclass. Use the {@link WorkoutFragment} factory method to create an instance of this
 * fragment.
 */
public class WorkoutFragment extends Fragment implements IApiListener, IPoseDataListener {
    private static final AtomicBoolean IS_CURRENTLY_PREDICTING = new AtomicBoolean(false);
    private final Exercise exercise;
    private PoseDataManager poseDataManager;
    private Skeleton skeleton;
    private MachineLearningApiHandler machineLearningApiHandler;
    private Context context;
    private PreviewView cameraSurface;

    public WorkoutFragment(Exercise exercise) {
        this.exercise = exercise;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        poseDataManager.removePoseDataListener(skeleton);
        machineLearningApiHandler.removeListener(this);
    }

    private void loadFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.relativeLayout, fragment)
                .addToBackStack(fragment.getTag())
                .commit();
    }

    @Override
    public void handleTaskCreationResponse(JsonObject jsonObject) {
        JsonObject task = jsonObject.getAsJsonObject("task");

        if (task != null) {
            String id = task.get("id").getAsString();
            machineLearningApiHandler.httpGetTaskResults(id);
        }
    }

    @Override
    public void handleTaskResultsResponse(JsonObject jsonObject) {
        Log.d(WorkoutFragment.class.getName(), jsonObject.toString());

        JsonObject task = jsonObject.getAsJsonObject("task");
        if (task.get("status").getAsString().equals("PENDING")) {
            String id = task.get("id").getAsString();
            machineLearningApiHandler.httpGetTaskResults(id);
        } else {
            poseDataManager.addPoseDataListener(this);

            MainActivity activity = (MainActivity) context;
            CameraManager cameraManager = activity.getCameraManager();
            cameraManager.start(cameraSurface);

            IS_CURRENTLY_PREDICTING.getAndSet(false);
        }
    }

    @Override
    public void poseAdded(Pose pose) {
        if (!IS_CURRENTLY_PREDICTING.getAndSet(true)) {
            // Predict the provided pose
            machineLearningApiHandler.httpPostPredict(exercise, pose);
        }
    }

    @Override
    public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped) {
        // Ignore
    }
}