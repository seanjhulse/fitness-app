package csuci.seanhulse.fitness.workouts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import csuci.seanhulse.fitness.MainActivity;
import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.camera.CameraManager;
import csuci.seanhulse.fitness.data.PoseDataManager;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.skeleton.Skeleton;

/**
 * A simple {@link Fragment} subclass. Use the {@link WorkoutFragment} factory method to create an instance of this
 * fragment.
 */
public class WorkoutFragment extends Fragment {
    private final Exercise exercise;
    private PoseDataManager poseDataManager;
    private Skeleton skeleton;

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
        Context context = view.getContext();
        MainActivity activity = (MainActivity) context;

        CameraManager cameraManager = activity.getCameraManager();

        this.poseDataManager = activity.getPoseDataManager();
        this.skeleton = view.findViewById(R.id.skeleton);
        poseDataManager.addPoseDataListener(skeleton);

        PreviewView cameraSurface = view.findViewById(R.id.cameraSurface);
        cameraManager.start(cameraSurface);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        poseDataManager.removePoseDataListener(skeleton);
    }

    private void loadFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.relativeLayout, fragment)
                .addToBackStack(fragment.getTag())
                .commit();
    }

}