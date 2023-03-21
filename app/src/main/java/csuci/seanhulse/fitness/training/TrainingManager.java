package csuci.seanhulse.fitness.training;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.room.Room;

import com.amplifyframework.core.Amplify;
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
import java.util.UUID;
import java.util.stream.Collectors;

import csuci.seanhulse.fitness.R;
import csuci.seanhulse.fitness.data.IPoseDataListener;
import csuci.seanhulse.fitness.db.Landmark;
import csuci.seanhulse.fitness.db.PoseDatabase;

/**
 * Manages the training overlay on the camera UI.
 *
 * @since 1.0.0
 */
public class TrainingManager extends LinearLayout implements IPoseDataListener {

    private static final int INITIAL_TRAINING_COUNTDOWN_SEC = 5;
    private static final int DEFAULT_TIME_BETWEEN_REPS = 3;
    private static final float IN_FRAME_LIKELIHOOD_VALUE = 0.90f;
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    private static final CharSequence finishedExercise = "Finished Exercise";
    private static final Gson gson = new Gson();
    private final PoseDatabase db;
    private final Context context;
    private CountDownTimer trainingTimer;
    private CountDownTimer clockTimer;
    private TextView startCountDownText;
    private TextView trainingIndicatorText;
    private String exerciseName = "DEFAULT";
    private TextView repCountDownText;
    // This should represent the most recent pose added by the PoseDataManager
    private Pose pose = null;
    private int numberOfReps = 10;

    enum RepState {UP, DOWN}

    private RepState repState = RepState.UP;

    public TrainingManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        this.context = context;

        // Setup the database
        this.db = Room
                .databaseBuilder(context, PoseDatabase.class, "pose-database")
                .fallbackToDestructiveMigration()
                .build();

        // TODO: Remove! Deletes the entire database whenever we re-run the training wrapper
        AsyncTask.execute(() -> {
            List<csuci.seanhulse.fitness.db.Pose> poses = this.db.poseDao().loadAll();
            poses.forEach(pose -> this.db.poseDao().delete(pose));
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.trainingIndicatorText = findViewById(R.id.trainingIndicator);
        this.startCountDownText = findViewById(R.id.trainingCountdownText);
        this.repCountDownText = findViewById(R.id.trainingRepsCountdownText);
    }

    public void startTrainingCountdown(String exerciseName) {
        this.exerciseName = exerciseName;
        clockTimer = createClockTimer(INITIAL_TRAINING_COUNTDOWN_SEC, true);
        clockTimer.start();
    }

    public void startTraining(FragmentManager fragmentManager) {
        DialogFragment trainingDialog = new TrainingDialog(this);
        trainingDialog.show(fragmentManager, "Training Dialog");
    }

    public void stopTraining() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }
        if (trainingTimer != null) {
            trainingTimer.cancel();
        }
    }

    @Override
    public void poseAdded(Pose pose) {
        this.pose = pose;
    }

    @Override
    public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped) {
        // IGNORE since we don't care about these values
    }

    private CountDownTimer createTrainingTimer(int requestedNumberOfReps) {
        this.numberOfReps = requestedNumberOfReps;
        final long millisecondsInFuture = numberOfReps * DEFAULT_TIME_BETWEEN_REPS * 1_000L * 2L;
        final long countDownInterval = DEFAULT_TIME_BETWEEN_REPS * 1_000L;

        return new CountDownTimer(millisecondsInFuture, countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                clockTimer = createClockTimer(DEFAULT_TIME_BETWEEN_REPS, false);
                clockTimer.start();

                trainingIndicatorText.setText(String.format("Hold the %s position of your exercise", repState));

                // Update the countdown value to the number of repetitions of an exercise left
                CharSequence remainingReps = String.valueOf(numberOfReps);
                repCountDownText.setText(String.format("%s reps remaining", remainingReps));

                repState = repState == RepState.UP ? RepState.DOWN : RepState.UP;

                if (repState == RepState.DOWN) {
                    numberOfReps = numberOfReps - 1;
                }

                createScreenshotAnimation();

                // If we have a pose stored, we should insert it into the db
                if (pose != null) {
                    try {
                        // Convert the landmarks and only insert the pose if the landmarks are not empty
                        List<Landmark> landmarks = createDbLandmarkFromPose(pose.getAllPoseLandmarks());
                        if (!landmarks.isEmpty()) {
                            String nowAsString = df.format(new Date());
                            csuci.seanhulse.fitness.db.Pose pose = new csuci.seanhulse.fitness.db.Pose(landmarks,
                                    nowAsString, repState.toString(), exerciseName);
                            pose.setId(UUID.randomUUID());

                            // Insert pose into the database
                            AsyncTask.execute(() -> {
                                db.poseDao().insert(pose);
                                db.poseDao().loadAll();

                                uploadPoseToS3(pose);
                            });
                        }
                    } catch (Exception e) {
                        Log.e("Database Insert", "onTick: ", e);
                    }
                }
            }

            @Override
            public void onFinish() {
                trainingIndicatorText.setText(finishedExercise);
                startCountDownText.setText("");
                repCountDownText.setText("");
            }
        };
    }

    private void uploadPoseToS3(csuci.seanhulse.fitness.db.Pose pose) {
        final File file = new File(context.getFilesDir(), pose.getId().toString());

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append(gson.toJson(pose));
            writer.close();
            Amplify.Storage.uploadFile(String.format("%s/%s", exerciseName, pose.getId()),
                    file,
                    result -> Log.i("Training upload", "Successfully uploaded: " + result.getKey()),
                    storageFailure -> Log.e("Training upload", "Upload failed", storageFailure)
            );
        } catch (Exception exception) {
            Log.e("Training upload", "Upload failed", exception);
        }

    }

    private List<Landmark> createDbLandmarkFromPose(List<PoseLandmark> poseLandmarks) {
        return poseLandmarks
                .stream()
                .filter(landmark -> landmark.getInFrameLikelihood() > IN_FRAME_LIKELIHOOD_VALUE)
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

    private CountDownTimer createClockTimer(int countdownSec, boolean startTrainingOnFinish) {
        return new CountDownTimer(countdownSec * 1_000L, 1_000L) {

            @Override
            public void onTick(long millisUntilFinished) {
                CharSequence remainingSeconds = String.valueOf((millisUntilFinished + 1_000L) / 1_000L);
                startCountDownText.setText(remainingSeconds);
            }

            @Override
            public void onFinish() {
                if (startTrainingOnFinish) {
                    trainingTimer = createTrainingTimer(10);
                    trainingTimer.start();
                }
            }
        };
    }

    private void createScreenshotAnimation() {
        ObjectAnimator colorFade = ObjectAnimator.ofObject(this, "backgroundColor",
                new ArgbEvaluator(),
                Color.argb(100, 255, 255, 255),
                Color.argb(0, 0, 0, 0));
        colorFade.setDuration(2_000);
        colorFade.start();
    }
}
