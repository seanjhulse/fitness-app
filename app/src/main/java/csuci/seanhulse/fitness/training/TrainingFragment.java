package csuci.seanhulse.fitness.training;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import csuci.seanhulse.fitness.MainActivity;
import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.camera.CameraManager;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.db.Level;
import csuci.seanhulse.fitness.db.PoseDatabase;

/**
 * A simple {@link Fragment} subclass. Use the {@link TrainingFragment} factory method to create an instance of this
 * fragment.
 */
public class TrainingFragment extends Fragment {

    private final Exercise exercise;
    private PoseDatabase db;
    private boolean isTraining = false;
    private Drawable startIcon;
    private Drawable stopIcon;
    private ColorStateList backgroundTintList;

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

        FloatingActionButton toggleTrainingButton = view.findViewById(R.id.toggleTrainingButton);
        toggleTrainingButton.setOnClickListener(this::toggleTraining);
        this.backgroundTintList = toggleTrainingButton.getBackgroundTintList();

        TextView trainingTitle = view.findViewById(R.id.trainingTitleText);
        TextView trainingRepsText = view.findViewById(R.id.trainingRepsText);

        trainingTitle.setText(exercise.getName());
        trainingRepsText.setText(String.format("%s reps", exercise.getReps()));

        this.db = Room.databaseBuilder(context, PoseDatabase.class,
                "pose-database").fallbackToDestructiveMigration().build();


        this.startIcon = AppCompatResources.getDrawable(context, R.drawable.baseline_play_arrow_24);
        this.stopIcon = AppCompatResources.getDrawable(context, R.drawable.baseline_stop_24);

        return view;
    }

    /**
     * Start the training based on the {@link Level} of the exercise.
     */
    private void toggleTraining(View view) {
        toggleTrainingButton(view);

        if (isTraining) {
            switch (exercise.getLevel()) {
                case LOW:
                    lowLevelTraining();
                    break;
                case MEDIUM:
                    mediumLevelTraining();
                    break;
                case HIGH:
                    highLevelTraining();
                    break;
            }
        } else {
            // Stop training
        }
    }

    /**
     * Toggle the floating action play/stop button UI.
     *
     * @param view the View for context to access the button
     */
    private void toggleTrainingButton(View view) {
        FloatingActionButton toggleTrainingButton = view.findViewById(R.id.toggleTrainingButton);
        isTraining = !isTraining;

        if (isTraining) {
            toggleTrainingButton.setImageDrawable(startIcon);
            toggleTrainingButton.setBackgroundTintList(backgroundTintList);
        } else {
            toggleTrainingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(view.getContext(),
                    R.color.design_default_color_error)));
            toggleTrainingButton.setImageDrawable(stopIcon);
        }
    }

    /**
     * Low level training is going to capture "UP" and "DOWN" information for the exercise in order to create the KNN
     * classifier for automatically detect that.
     */
    private void lowLevelTraining() {

    }

    /**
     * Medium level training is going to try to capture the exercise and the transitions between "UP" and "DOWN"
     * information.
     */
    private void mediumLevelTraining() {
    }

    /**
     * High level training is going to refine the transition and pose data even further than medium training.
     */
    private void highLevelTraining() {
    }

}