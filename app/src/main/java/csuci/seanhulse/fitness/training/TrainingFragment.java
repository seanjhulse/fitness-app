package csuci.seanhulse.fitness.training;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.StorageException;
import com.amplifyframework.storage.result.StorageUploadFileResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import csuci.seanhulse.fitness.MainActivity;
import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.camera.CameraManager;
import csuci.seanhulse.fitness.data.PoseDataManager;
import csuci.seanhulse.fitness.db.Exercise;
import csuci.seanhulse.fitness.db.Landmark;
import csuci.seanhulse.fitness.db.Level;
import csuci.seanhulse.fitness.db.PoseDatabase;

/**
 * A simple {@link Fragment} subclass. Use the {@link TrainingFragment} factory method to create an instance of this
 * fragment.
 */
public class TrainingFragment extends Fragment {
    public static final int INTERVAL_SEC = 2;
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    private static final Gson gson = new Gson();
    private final Exercise exercise;
    private final ProgressBarRunnable progressBarRunnable = new ProgressBarRunnable(new Handler());
    private boolean isTraining = false;
    private Drawable startIcon;
    private Drawable stopIcon;
    private ColorStateList backgroundTintList;
    private CountDownTimer countDownTimer;
    private TextView progressText;
    private RepState repState = RepState.DOWN;
    private long countDownTimeMs = -1L;
    private int numRepsRemaining = -1;
    private TextView trainingRepsText;
    private PoseDataManager poseDataManager;
    private PoseDatabase db;
    private Context context;

    private enum RepState {UP, DOWN}

    public TrainingFragment(Exercise exercise) {
        this.exercise = exercise;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.context = inflater.getContext();

        // Setup the database
        this.db = Room
                .databaseBuilder(context, PoseDatabase.class, "pose-database")
                .fallbackToDestructiveMigration()
                .build();

        MainActivity activity = (MainActivity) context;
        CameraManager cameraManager = activity.getCameraManager();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_training, container, false);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBarRunnable.setProgressBar(progressBar);
        progressText = view.findViewById(R.id.progressText);

        PreviewView cameraSurface = view.findViewById(R.id.cameraSurface);
        cameraManager.start(cameraSurface);

        FloatingActionButton toggleTrainingButton = view.findViewById(R.id.toggleTrainingButton);
        toggleTrainingButton.setOnClickListener(this::toggleTraining);
        backgroundTintList = toggleTrainingButton.getBackgroundTintList();

        TextView trainingTitle = view.findViewById(R.id.trainingTitleText);
        trainingTitle.setText(exercise.getName());

        trainingRepsText = view.findViewById(R.id.trainingRepsText);
        trainingRepsText.setText(String.format("%s reps", exercise.getReps()));

        poseDataManager = ((MainActivity) context).getPoseDataManager();

        this.startIcon = AppCompatResources.getDrawable(context, R.drawable.baseline_play_arrow_24);
        this.stopIcon = AppCompatResources.getDrawable(context, R.drawable.baseline_stop_24);

        return view;
    }

    /**
     * Start the training based on the {@link Level} of the exercise.
     *
     * @param view the {@link View} with which we are drawing on
     */
    private void toggleTraining(View view) {
        isTraining = !isTraining;

        toggleTrainingButton(view);

        if (isTraining) {
            switch (exercise.getLevel()) {
                case LOW:
                    lowLevelTraining(view);
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
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        }
    }

    /**
     * Toggle the floating action play/stop button UI.
     *
     * @param view the View for context to access the button
     */
    private void toggleTrainingButton(View view) {
        final FloatingActionButton toggleTrainingButton = view.findViewById(R.id.toggleTrainingButton);

        if (!isTraining) {
            toggleTrainingButton.setImageDrawable(startIcon);
            toggleTrainingButton.setBackgroundTintList(backgroundTintList);
        } else {
            final int backgroundColor = ContextCompat.getColor(view.getContext(), R.color.design_default_color_error);
            final ColorStateList backgroundTint = ColorStateList.valueOf(backgroundColor);
            toggleTrainingButton.setBackgroundTintList(backgroundTint);
            toggleTrainingButton.setImageDrawable(stopIcon);

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            if (progressBarRunnable.isRunning()) {
                progressBarRunnable.cancel();
            }
        }
    }

    /**
     * Low level training is going to capture "UP" and "DOWN" information for the exercise in order to create the KNN
     * classifier for automatically detect that.
     *
     * @param view the {@link View} with which we are drawing on
     */
    private void lowLevelTraining(View view) {
        repState = RepState.DOWN;

        // Set the number of reps remaining if we haven't canceled our timer
        if (numRepsRemaining < 0 || countDownTimeMs < 0) {
            numRepsRemaining = exercise.getReps();
        }

        // # of reps remaining * interval for each up/down state * 2 (because we are counting up AND down)
        final int repCountdownSec = numRepsRemaining * INTERVAL_SEC * 2;
        countDownTimer = createUpDownCountdownTimer(view, repCountdownSec);
        countDownTimer.start();
    }

    /**
     * Create an "Up and Down" countdown timer, which counts down the reps and the number of time between up and down
     * reps.
     *
     * @param repCountdownSec the number of seconds per Up or Down part of the rep
     * @return the {@link CountDownTimer}
     */
    private CountDownTimer createUpDownCountdownTimer(View view, int repCountdownSec) {
        return new CountDownTimer(repCountdownSec * 1_000L, INTERVAL_SEC * 1_000L) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (isTraining) {
                    countDownTimeMs = millisUntilFinished;

                    repState = repState == RepState.UP ? RepState.DOWN : RepState.UP;

                    savePose(poseDataManager.getPose());

                    progressText.setText(repState.toString());

                    if (repState == RepState.UP) {
                        trainingRepsText.setText(String.format("%s reps", numRepsRemaining));
                        numRepsRemaining = numRepsRemaining - 1;
                    }

                    progressBarRunnable.setDelay(INTERVAL_SEC);
                    progressBarRunnable.start();
                }
            }

            @Override
            public void onFinish() {
                trainingRepsText.setText(R.string.finishedTrainingRepsText);
                progressText.setText("---");
                progressBarRunnable.cancel();
                toggleTraining(view);
            }
        };
    }

    /**
     * Saves the provided {@link Pose} to the database.
     *
     * @param mlPose {@link Pose} the ML pose created by Google's ML Kit
     */
    private void savePose(Pose mlPose) {
        AsyncTask.execute(() -> {
            List<Landmark> landmarks = createDbLandmarkFromPose(mlPose.getAllPoseLandmarks());

            csuci.seanhulse.fitness.db.Pose pose = new csuci.seanhulse.fitness.db.Pose(
                    landmarks,
                    df.format(new Date()),
                    repState.toString(),
                    exercise.getId()
            );

            long id = db.poseDao().insert(pose);
            pose.setId(id);

            uploadPoseToS3(pose);
        });
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

    private List<Landmark> createDbLandmarkFromPose(List<PoseLandmark> poseLandmarks) {
        return poseLandmarks
                .stream()
                .map(landmark -> {
                    PointF3D position = landmark.getPosition3D();
                    return new Landmark(position.getX(),
                            position.getY(),
                            position.getZ(),
                            landmark.getLandmarkType(),
                            landmark.getInFrameLikelihood());
                })
                .collect(Collectors.toList());
    }

    /**
     * Uploads a {@link csuci.seanhulse.fitness.db.Pose} to an S3 bucket.
     *
     * @param pose the pose we are uploading
     */
    private void uploadPoseToS3(csuci.seanhulse.fitness.db.Pose pose) {
        final File file = new File(context.getFilesDir(), String.valueOf(pose.getId()));

        try {
            String exerciseName = exercise.getName().toLowerCase();
            long exerciseId = exercise.getId();
            String fileName = String.format("%s-%s/%s.json", exerciseName, exerciseId, pose.getId());

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append(gson.toJson(pose));
            writer.close();

            Amplify.Storage.uploadFile(fileName, file, this::getTrainingUpload, this::uploadError);
        } catch (Exception exception) {
            Log.e("Training upload", "Upload failed", exception);
        }

    }

    /**
     * Handler for upload errors to the Amplify AWS S3 bucket.
     *
     * @param storageFailure the exception during upload
     */
    private void uploadError(StorageException storageFailure) {
        Log.e("Training upload", "Upload failed", storageFailure);
    }

    /**
     * Handler for successful uploads to the Amplify AWS S3 bucket.
     *
     * @param result the response on a successful upload
     */
    private void getTrainingUpload(StorageUploadFileResult result) {
        Log.i("Training upload", "Successfully uploaded: " + result.getKey());
    }

    /**
     * Progress bar which counts down until the progress reaches "100". The progress counts at a rate of delayMs.
     */
    private static class ProgressBarRunnable implements Runnable {
        private final Handler handler;
        private long delayMs;
        private int progress = 0;
        private boolean isRunning = false;
        private ProgressBar progressBar;

        private ProgressBarRunnable(Handler handler) {
            this.handler = handler;
        }

        public void start() {
            if (isRunning()) {
                cancel();
            }

            handler.post(this);
            isRunning = true;
        }

        public void setDelay(int delaySec) {
            long milliseconds = (delaySec * 1_000L);
            // Subtract just a little to let the progress bar finish before starting the next one
            this.delayMs = (milliseconds / 100) - 2L;
        }

        @Override
        public void run() {
            if (progress <= 100) {
                progressBar.setProgress(progress);
                progress++;
                handler.postDelayed(this, delayMs);
            } else {
                progress = 0;
                progressBar.setProgress(progress);
                handler.removeCallbacks(this);
                isRunning = false;
            }
        }

        public void cancel() {
            handler.removeCallbacks(this);
            progress = 0;
            isRunning = false;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setProgressBar(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }
    }

}