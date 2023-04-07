package csuci.seanhulse.fitness.workouts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.training.TrainingFragment;
import csuci.seanhulse.fitness.utility.DatetimeFormatter;

/**
 * A simple {@link Fragment} subclass. Use the {@link ExerciseFragment} factory method to create an instance of this
 * fragment.
 */
public class ExerciseFragment extends Fragment {
    private final Exercise exercise;

    public ExerciseFragment(Exercise exercise) {
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
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);
        setupExerciseTitle(view);

        AppCompatImageView trainingButton = view.findViewById(R.id.trainButton);
        trainingButton.setOnClickListener(this::startTraining);

        AppCompatImageView workoutButton = view.findViewById(R.id.workoutButton);
        workoutButton.setOnClickListener(this::startWorkout);
        return view;
    }

    private void startWorkout(View view) {
        loadFragment(new WorkoutFragment(exercise));
    }

    private void startTraining(View view) {
        loadFragment(new TrainingFragment(exercise));
    }

    private void setupExerciseTitle(View view) {
        TextView exerciseNameTextView = view.findViewById(R.id.exerciseName);
        exerciseNameTextView.setText(exercise.getName());

        TextView exerciseReps = view.findViewById(R.id.exerciseReps);
        exerciseReps.setText(String.format("%s reps", exercise.getReps()));

        TextView exerciseDatetime = view.findViewById(R.id.exerciseDatetime);
        String exerciseDatetimeFormatted = DatetimeFormatter.datetimeFormatter(exercise.getDatetime());
        exerciseDatetime.setText(exerciseDatetimeFormatted);
    }

    private void loadFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.relativeLayout, fragment)
                .addToBackStack(fragment.getTag())
                .commit();
    }

}