package csuci.seanhulse.fitness.training;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import csuci.seanhulse.fitness.R;

public class TrainingDialog extends DialogFragment {
    private final TrainingManager trainingManager;

    public TrainingDialog(TrainingManager trainingManager) {
        this.trainingManager = trainingManager;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        if (activity != null) {
            final View view = inflater.inflate(R.layout.dialog_training, null);
            final EditText exerciseNameField = view.findViewById(R.id.exerciseName);

            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton(R.string.submit, (dialog, id) -> {
                        Log.d("Training Dialog", String.valueOf(dialog));
                        trainingManager.startTrainingCountdown(String.valueOf(exerciseNameField.getText()));
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) ->
                            TrainingDialog.this.getDialog().cancel());
        }

        return builder.create();
    }

}
