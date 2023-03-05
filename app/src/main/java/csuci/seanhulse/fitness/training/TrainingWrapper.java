package csuci.seanhulse.fitness.training;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.UiThread;

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
    private final Timer timer = new Timer();
    private TextView countDownText;
    private int countDownValue;
    private TextView trainingIndicatorText;

    public TrainingWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.trainingIndicatorText = findViewById(R.id.trainingIndicator);
        this.countDownText = findViewById(R.id.trainingCountdownText);
    }

    public void startTraining() {
        runCountdownTimer(INITIAL_TRAINING_COUNTDOWN_SEC);
    }

    public void stopTraining() {
        timer.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (countDownValue > 0) {
            // Update the countdown value
            countDownText.setVisibility(VISIBLE);
            CharSequence charSequence = String.valueOf(countDownValue);
            countDownText.setText(charSequence);
        }
    }

    private void runCountdownTimer(int numberOfSeconds) {
        countDownValue = numberOfSeconds;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (numberOfSeconds > 0) {
                    runCountdownTimer(numberOfSeconds - 1);

                    // Force redraw of the view
                    invalidate();
                }
            }
        }, 1_000);
    }
}
