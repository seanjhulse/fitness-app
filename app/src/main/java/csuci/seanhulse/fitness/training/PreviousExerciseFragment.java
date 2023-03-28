package csuci.seanhulse.fitness.training;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.db.PoseDatabase;

/**
 * A simple {@link Fragment} subclass. Use the {@link PreviousExerciseFragment} factory method to create an instance of
 * this fragment.
 */
public class PreviousExerciseFragment extends Fragment {
    private PoseDatabase db;
    private Collection<Exercise> exercises;
    private Exercise checkedExercise;

    public PreviousExerciseFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_previous_exercise, container, false);

        Context context = inflater.getContext();
        this.db = Room.databaseBuilder(context, PoseDatabase.class,
                "pose-database").fallbackToDestructiveMigration().build();

        initializeExerciseButtons(view);
        return view;
    }

    private void loadTrainingScreen(View view) {
        if (checkedExercise != null) {
            loadFragment(new TrainingFragment(checkedExercise));
        }
    }

    /**
     * Gets the Exercises from the databases and creates a radio button for each one.
     *
     * @param view the root view to access elements and context
     */
    private void initializeExerciseButtons(View view) {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        // Runs off the UI thread
        executor.execute(() -> {
            exercises = db.exerciseDao().loadAll();
            RadioGroup previousExercisesRadioGroup = view.findViewById(R.id.previousExercisesRadioGroup);

            // Runs on the UI thread
            handler.post(() -> {
                exercises.forEach(exercise -> {
                    RadioButton radioButton = new RadioButton(view.getContext());
                    String exerciseText = String.format("%s - %s reps", exercise.getName(), exercise.getReps());
                    radioButton.setText(exerciseText);
                    radioButton.setTextSize(18);
                    radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            checkedExercise = exercise;
                        }
                    });
                    previousExercisesRadioGroup.addView(radioButton);
                });

                // Initialize the "start" button
                Button button = view.findViewById(R.id.startTrainingExerciseButton);
                button.setOnClickListener(this::loadTrainingScreen);
                button.setEnabled(!exercises.isEmpty());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    button.setAllowClickWhenDisabled(false);
                }
            });
        });
    }

    void loadFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction().replace(R.id.relativeLayout, fragment).addToBackStack(
                fragment.getTag()).commit();
    }

}