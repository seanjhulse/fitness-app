package csuci.seanhulse.fitness.training;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public PreviousExerciseFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_previous_exercise, container, false);

        Context context = inflater.getContext();
        PoseDatabase db = Room
                .databaseBuilder(context, PoseDatabase.class, "pose-database")
                .fallbackToDestructiveMigration()
                .build();

        Executor executor = Executors.newSingleThreadExecutor();

        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here

        });
        executor.execute(() -> {
            exercises = db.exerciseDao().loadAll();
            RadioGroup previousExercisesRadioGroup = view.findViewById(R.id.previousExercisesRadioGroup);

            handler.post(() -> {
                exercises.forEach(exercise -> {
                    RadioButton radioButton = new RadioButton(view.getContext());
                    radioButton.setText(exercise.getName());
                    previousExercisesRadioGroup.addView(radioButton);
                });
            });

        });

        return view;
    }

}