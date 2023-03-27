package csuci.seanhulse.fitness.training;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.db.PoseDatabase;

/**
 * A simple {@link Fragment} subclass. Use the {@link NewExerciseFragment} factory method to create an instance of this
 * fragment.
 */
public class NewExerciseFragment extends Fragment {
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    private PoseDatabase db;

    public NewExerciseFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_exercise, container, false);

        Button createExerciseButton = view.findViewById(R.id.createNewExerciseButton);
        createExerciseButton.setOnClickListener(this::createExerciseEvent);

        Context context = inflater.getContext();

        this.db = Room
                .databaseBuilder(context, PoseDatabase.class, "pose-database")
                .fallbackToDestructiveMigration()
                .build();

        return view;
    }

    private void createExerciseEvent(View view) {
        TextView exerciseNameTextView = view.getRootView().findViewById(R.id.exerciseName);
        TextView exerciseRepsPerSetTextView = view.getRootView().findViewById(R.id.exerciseRepsPerSet);

        String name = String.valueOf(exerciseNameTextView.getText());
        String reps = exerciseRepsPerSetTextView.getText().toString();

        if (name.isBlank() || reps.isBlank()) {
            Log.w("Create Exercise Event", "Exercise name or reps were blank");
            return;
        }

        saveExercise(name, Integer.parseInt(reps));

        final FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            fragmentActivity.getSupportFragmentManager().popBackStack();
        }
    }

    private void saveExercise(String name, int reps) {
        String nowAsString = df.format(new Date());
        Exercise exercise = new Exercise(nowAsString, name, reps);
        AsyncTask.execute(() -> db.exerciseDao().insert(exercise));
    }

}