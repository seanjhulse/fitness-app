package csuci.seanhulse.fitness.training;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import csuci.seanhulse.fitness.R;

/**
 * Manages the training overlay on the camera UI.
 *
 * @since 1.0.0
 */
public class TrainingWrapper extends LinearLayout {

    private static final int INITIAL_TRAINING_COUNTDOWN_SEC = 5;
    private static final int DEFAULT_TIME_BETWEEN_REPS = 3;
    private final CountDownTimer trainingTimer = createTrainingTimer(10);
    private CountDownTimer clockTimer = createClockTimer(INITIAL_TRAINING_COUNTDOWN_SEC, true);
    private TextView startCountDownText;
    private TextView trainingIndicatorText;
    private TextView repCountDownText;

    private int numberOfReps = 10;

    enum RepState {UP, DOWN}

    private RepState repState = RepState.UP;

    public TrainingWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.trainingIndicatorText = findViewById(R.id.trainingIndicator);
        this.startCountDownText = findViewById(R.id.trainingCountdownText);
        this.repCountDownText = findViewById(R.id.trainingRepsCountdownText);
    }

    public void startTrainingCountdown() {
        clockTimer.start();
    }

    public void stopTraining() {
        clockTimer.cancel();
        trainingTimer.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {

    }

    private CountDownTimer createTrainingTimer(int requestedNumberOfReps) {
        this.numberOfReps = requestedNumberOfReps;
        return new CountDownTimer(numberOfReps * DEFAULT_TIME_BETWEEN_REPS * 1_000L * 2L, DEFAULT_TIME_BETWEEN_REPS * 1_000L) {

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
            }

            @Override
            public void onFinish() {
                trainingIndicatorText.setText("Finished Exercise");
                startCountDownText.setText("");
            }
        };
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
                    trainingTimer.start();
                }
            }
        };
    }

    private void createScreenshotAnimation() {
        ObjectAnimator colorFade = ObjectAnimator.ofObject(this, "backgroundColor",
                new ArgbEvaluator(),
                Color.argb(100,255,255,255),
                Color.argb(0,0,0,0));
        colorFade.setDuration(2_000);
        colorFade.start();
    }
}
