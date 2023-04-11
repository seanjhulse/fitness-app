package csuci.seanhulse.fitness.workouts;

import static com.google.android.material.R.id.snackbar_text;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;
import static csuci.seanhulse.fitness.api.TaskState.Status.FAILURE;
import static csuci.seanhulse.fitness.api.TaskState.Status.PENDING;
import static csuci.seanhulse.fitness.api.TaskState.Status.SUCCESS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.api.IApiListener;
import csuci.seanhulse.fitness.api.MachineLearningApiHandler;
import csuci.seanhulse.fitness.api.TaskState.Status;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.db.Model;
import csuci.seanhulse.fitness.db.Pose;
import csuci.seanhulse.fitness.db.PoseDatabase;
import csuci.seanhulse.fitness.training.TrainingFragment;
import csuci.seanhulse.fitness.utility.DatetimeFormatter;

/**
 * A simple {@link Fragment} subclass. Use the {@link ExerciseFragment} factory method to create an instance of this
 * fragment.
 */
public class ExerciseFragment extends Fragment implements IApiListener {
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    public static final int MINIMUM_REQUIRED_POSES = 20;
    private final Exercise exercise;
    private PoseDatabase db;
    private MachineLearningApiHandler machineLearningApiHandler;
    private View coordinatorLayout;
    private View workoutButton;
    private View buildMlModelButton;
    private Context context;

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
        context = view.getContext();

        this.db = Room
                .databaseBuilder(context, PoseDatabase.class, "pose-database")
                .fallbackToDestructiveMigration()
                .build();

        machineLearningApiHandler = new MachineLearningApiHandler(context);
        machineLearningApiHandler.addListener(this);

        setupExerciseTitle(view);

        AppCompatImageView trainingButton = view.findViewById(R.id.trainButton);
        trainingButton.setOnClickListener(this::startTraining);

        buildMlModelButton = view.findViewById(R.id.buildMlModelButton);
        buildMlModelButton.setOnClickListener(this::triggerModelBuild);

        workoutButton = view.findViewById(R.id.workoutButton);
        workoutButton.setOnClickListener(this::startWorkout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            workoutButton.setAllowClickWhenDisabled(true);
            buildMlModelButton.setAllowClickWhenDisabled(true);
        }

        // Disable the buttons by default
        workoutButton.setEnabled(false);
        buildMlModelButton.setEnabled(false);

        Handler handler = new Handler();
        AsyncTask.execute(() -> {
            List<Model> mlModels = db.modelDao().findAll(exercise.getId());
            List<Pose> poses = db.poseDao().findAll(exercise.getId());
            handler.post(() -> {
                workoutButton.setEnabled(mlModels.size() > 0);
                buildMlModelButton.setEnabled(poses.size() > MINIMUM_REQUIRED_POSES);
            });
        });

        this.coordinatorLayout = view.findViewById(R.id.exerciseCoordinatorLayout);


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        machineLearningApiHandler.removeListener(this);
    }

    private void triggerModelBuild(View view) {
        if (!buildMlModelButton.isEnabled()) {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.disabled_ml_model_message, LENGTH_LONG);
            snackbar.setBackgroundTint(getResources().getColor(R.color.colorPrimaryVariant, context.getTheme()));
            TextView textview = snackbar.getView().findViewById(snackbar_text);
            textview.setTextSize(20);
            snackbar.show();
        } else {
            machineLearningApiHandler.httpPostBuildModel(exercise);
        }
    }

    private void startWorkout(View view) {
        if (!workoutButton.isEnabled()) {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.workout_disabled_message, LENGTH_LONG);
            snackbar.setBackgroundTint(getResources().getColor(R.color.colorPrimaryVariant, context.getTheme()));
            TextView textview = snackbar.getView().findViewById(snackbar_text);
            textview.setTextSize(20);
            snackbar.show();
        } else {
            loadFragment(new WorkoutFragment(exercise));
        }
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

    @Override
    public void handleTaskCreationResponse(JsonObject jsonObject) {
        JsonObject task = jsonObject.getAsJsonObject("task");

        if (task != null) {
            String id = task.get("id").getAsString();
            machineLearningApiHandler.httpGetTaskResults(id);
            AsyncTask.execute(() -> {
                Model model = new Model();
                model.setTaskId(id);

                String nowAsString = df.format(new Date());
                model.setDatetime(nowAsString);

                String name = String.format("%s-%s", exercise.getName().toLowerCase(), exercise.getId());
                model.setName(name);

                model.setExerciseId(exercise.getId());

                db.modelDao().insert(model);
            });
        }
    }

    @Override
    public void handleTaskResultsResponse(JsonObject jsonObject) {
        JsonObject task = jsonObject.getAsJsonObject("task");
        Status status = Status.valueOf(task.get("status").getAsString());
        String id = task.get("id").getAsString();

        if (status.equals(PENDING)) {
            machineLearningApiHandler.httpGetTaskResults(id);
        } else {
            AsyncTask.execute(() -> {
                Optional<Model> modelOptional = db.modelDao().loadAll()
                        .stream()
                        .filter(model -> model.getTaskId().equals(id))
                        .findFirst();
                if (modelOptional.isPresent()) {
                    Model model = modelOptional.get();
                    db.modelDao().updateStatus(status, model.getId());
                }
            });

            if (status.equals(SUCCESS)) {
                Log.i(ExerciseFragment.class.getName(), "Task was successful");
                workoutButton.setEnabled(true);

            } else if (status.equals(FAILURE)) {
                Log.i(ExerciseFragment.class.getName(), "Task was unsuccessful");
            }
        }

    }
}