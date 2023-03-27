package csuci.seanhulse.fitness.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import csuci.seanhulse.fitness.R;

/**
 * A simple {@link Fragment} subclass. Use the {@link TrainingMenuFragment} factory method to create an instance of this
 * fragment.
 */
public class TrainingMenuFragment extends Fragment {

    public TrainingMenuFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_training_menu, container, false);
        Button previousTrainingExercise = view.findViewById(R.id.trainPreviousExerciseButton);
        previousTrainingExercise.setOnClickListener(v -> {
            loadFragment(new PreviousExerciseFragment());
        });
        Button newTrainingExercise = view.findViewById(R.id.trainNewExerciseButton);
        newTrainingExercise.setOnClickListener(v -> {
            loadFragment(new NewExerciseFragment());
        });
        return view;
    }

    void loadFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.relativeLayout, fragment)
                .addToBackStack(fragment.getTag())
                .commit();
    }
}