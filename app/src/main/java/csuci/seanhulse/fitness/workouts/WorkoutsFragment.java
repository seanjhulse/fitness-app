package csuci.seanhulse.fitness.workouts;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;

import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.db.PoseDatabase;
import csuci.seanhulse.fitness.training.NewExerciseFragment;
import csuci.seanhulse.fitness.utility.DatetimeFormatter;

/**
 * A simple {@link Fragment} subclass. Use the {@link WorkoutsFragment} factory method to create an instance of this
 * fragment.
 */
public class WorkoutsFragment extends Fragment {
    public WorkoutsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workouts, container, false);
        Context context = view.getContext();

        // Setup the database
        PoseDatabase db = Room
                .databaseBuilder(context, PoseDatabase.class, "pose-database")
                .fallbackToDestructiveMigration()
                .build();

        FloatingActionButton createExerciseButton = view.findViewById(R.id.createExerciseButton);
        createExerciseButton.setOnClickListener(this::createNewExercise);

        Handler handler = new Handler();
        AsyncTask.execute(() -> {
            Collection<Exercise> exercises = db.exerciseDao().loadAll();
            handler.post(() -> {
                LinearLayout linearLayout = view.findViewById(R.id.workoutsContainer);
                Collection<View> buttons = createExerciseButtons(context, exercises);
                buttons.forEach(linearLayout::addView);
            });
        });

        return view;
    }

    private void createNewExercise(View view) {
        loadFragment(new NewExerciseFragment());
    }

    private Collection<View> createExerciseButtons(Context context, Collection<Exercise> exercises) {
        final LinearLayout.LayoutParams workoutContainerLayoutParams =
                new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        workoutContainerLayoutParams.setMargins(0, 20, 0, 20);

        Collection<View> buttons = new ArrayList<>();
        for (Exercise exercise : exercises) {
            LinearLayout workout = new LinearLayout(context);
            workout.setOnClickListener(view -> setExerciseFragment(exercise));
            workout.setBackgroundResource(R.drawable.back);
            workout.setOrientation(LinearLayout.VERTICAL);
            workout.setLayoutParams(workoutContainerLayoutParams);
            workout.setPadding(20, 20, 20, 20);

            View nameContainer = createNameContainer(context, exercise);
            workout.addView(nameContainer);

            View bottomDetailsContainer = createBottomDetailsContainer(context, exercise);
            workout.addView(bottomDetailsContainer);

            buttons.add(workout);
        }
        return buttons;
    }

    @NonNull
    private View createNameContainer(Context context, Exercise exercise) {
        final LinearLayout.LayoutParams nameContainerLayoutParams =
                new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);

        LinearLayout nameContainer = new LinearLayout(context);
        nameContainer.setOrientation(LinearLayout.HORIZONTAL);
        nameContainer.setLayoutParams(nameContainerLayoutParams);
        nameContainer.setPadding(0, 0, 0, 50);

        TextView name = new TextView(context);
        name.setText(exercise.getName());
        name.setTextSize(20);

        final RelativeLayout.LayoutParams repsLayoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT,
                MATCH_PARENT);
        repsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        TextView reps = new TextView(context);
        reps.setLayoutParams(repsLayoutParams);
        reps.setText(String.format("%s reps", exercise.getReps()));
        reps.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        reps.setTextSize(20);

        nameContainer.addView(name);
        nameContainer.addView(reps);
        return nameContainer;
    }

    @NonNull
    private View createBottomDetailsContainer(Context context, Exercise exercise) {
        final LinearLayout.LayoutParams bottomDetailsContainerLayoutParams =
                new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);

        LinearLayout bottomDetailsContainer = new LinearLayout(context);
        bottomDetailsContainer.setOrientation(LinearLayout.HORIZONTAL);
        bottomDetailsContainer.setLayoutParams(bottomDetailsContainerLayoutParams);

        TextView datetime = new TextView(context);
        String exerciseDatetimeFormatted = DatetimeFormatter.datetimeFormatter(exercise.getDatetime());
        datetime.setText(exerciseDatetimeFormatted);

        bottomDetailsContainer.addView(datetime);

        final RelativeLayout.LayoutParams levelLayoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT,
                MATCH_PARENT);
        levelLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        TextView level = new TextView(context);
        level.setLayoutParams(levelLayoutParams);
        level.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        level.setText(String.format("Training Level: %s", exercise.getLevel().getName()));
        bottomDetailsContainer.addView(level);

        return bottomDetailsContainer;
    }

    private void setExerciseFragment(Exercise exercise) {
        loadFragment(new ExerciseFragment(exercise));
    }

    private void loadFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.relativeLayout, fragment)
                .addToBackStack(fragment.getTag())
                .commit();
    }

}