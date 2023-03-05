package csuci.seanhulse.fitness.training;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

import csuci.seanhulse.fitness.R;

/**
 * Manages the training overlay on the camera UI.
 *
 * @since 1.0.0
 */
public class TrainingWrapper extends LinearLayout {

    private static final int INITIAL_TRAINING_COUNTDOWN_SEC = 3;
    private static final int INITIAL_TRAINING_REPETITIONS = 6;
    private final Timer countdownTimer = new Timer();
    private final Timer repTimer = new Timer();
    private TextView startCountDownText;
    private TextView trainingIndicatorText;
    private TextView repCountDownText;
    private int countDownValue;
    private int repsValue = -1;

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
        runCountdownTimer(INITIAL_TRAINING_COUNTDOWN_SEC);

        // Create a timer to being the training session
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runTrainingTimer(INITIAL_TRAINING_REPETITIONS);
            }
        }, INITIAL_TRAINING_COUNTDOWN_SEC * 1_000);
    }

    public void stopTraining() {
        countdownTimer.cancel();
        repTimer.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (countDownValue >= 0) {
            // Update the countdown value to the number of seconds left
            CharSequence remainingSeconds = String.valueOf(countDownValue);
            startCountDownText.setText(remainingSeconds);
        }

        if (repsValue >= 0) {
            repCountDownText.setVisibility(VISIBLE);

            // Update the countdown value to the number of repetitions of an exercise left
            CharSequence remainingReps = String.valueOf(repsValue);
            repCountDownText.setText(String.format("%s reps remaining", remainingReps));
        }

        if (repState == RepState.UP && repsValue > 0) {
            trainingIndicatorText.setText("Up");
        }

        if (repState == RepState.DOWN && repsValue > 0) {
            trainingIndicatorText.setText("Down");
        }

    }

    private void runTrainingTimer(int numberOfRepetitions) {
        repsValue = numberOfRepetitions;

        countDownValue = INITIAL_TRAINING_COUNTDOWN_SEC;
        runCountdownTimer(countDownValue);

        repTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (numberOfRepetitions > 0) {
                    if (repState == RepState.UP) {
                        // Toggle the rep state
                        repState = RepState.DOWN;

                        // We've finished a repetition and need to move down to perform another
                        runTrainingTimer(numberOfRepetitions - 1);
                    } else {
                        // Toggle the rep state
                        repState = RepState.UP;

                        // We're halfway through a repetition and need to move up to finish it
                        runTrainingTimer(numberOfRepetitions);
                    }

                    // Force redraw of the view
                    invalidate();
                }
            }
        }, 3_000);
    }

    private void runCountdownTimer(int numberOfSeconds) {
        countDownValue = numberOfSeconds;

        countdownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (numberOfSeconds > 0) {
                    runCountdownTimer(numberOfSeconds - 1);
                }

                // Force redraw of the view
                invalidate();
            }
        }, 1_000);
    }
}
