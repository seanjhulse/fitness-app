package csuci.seanhulse.fitness.training;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import csuci.seanhulse.fitness.MainActivity;
import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.camera.CameraManager;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.db.PoseDatabase;

/**
 * A simple {@link Fragment} subclass. Use the {@link TrainingFragment} factory method to create an instance of this
 * fragment.
 */
public class TrainingFragment extends Fragment {

    private final Exercise exercise;
    private PoseDatabase db;

    public TrainingFragment(Exercise exercise) {
        this.exercise = exercise;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = inflater.getContext();

        MainActivity activity = (MainActivity) context;
        CameraManager cameraManager = activity.getCameraManager();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_training, container, false);

        PreviewView cameraSurface = view.findViewById(R.id.cameraSurface);
        cameraManager.start(cameraSurface);

        TextView trainingTitle = view.findViewById(R.id.trainingTitleText);
        TextView trainingRepsText = view.findViewById(R.id.trainingRepsText);

        trainingTitle.setText(exercise.getName());
        trainingRepsText.setText(String.format("%s reps", exercise.getReps()));

        this.db = Room.databaseBuilder(context, PoseDatabase.class,
                "pose-database").fallbackToDestructiveMigration().build();

        return view;
    }


}