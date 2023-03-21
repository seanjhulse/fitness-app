package csuci.seanhulse.fitness;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.logging.AndroidLoggingPlugin;
import com.amplifyframework.logging.LogLevel;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import csuci.seanhulse.fitness.camera.CameraManager;
import csuci.seanhulse.fitness.data.PoseDataManager;
import csuci.seanhulse.fitness.databinding.ActivityMainBinding;
import csuci.seanhulse.fitness.training.TrainingManager;

public class MainActivity extends AppCompatActivity {
    private ViewFlipper viewFlipper;
    private View cameraLayout;
    private View homePage;
    private TrainingManager trainingManager;
    private CameraManager cameraManager;
    private Context applicationContext;
    private ActivityMainBinding binding;
    private final PoseDataManager poseDataManager = new PoseDataManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        this.applicationContext = this.getApplicationContext();
        this.viewFlipper = findViewById(R.id.viewFlipper);
        this.cameraLayout = viewFlipper.findViewById(R.id.cameraLayout);
        this.homePage = viewFlipper.findViewById(R.id.homepage);
        this.trainingManager = viewFlipper.findViewById(R.id.trainingManager);

        MaterialButton defaultSquatButton = viewFlipper.findViewById(R.id.defaultSquatButton);
        defaultSquatButton.setOnClickListener(this::startExercising);

        ImageButton openHomepageButton = viewFlipper.findViewById(R.id.openHomepageButton);
        openHomepageButton.setOnClickListener(this::openHomepage);

        FloatingActionButton startTrainingButton = viewFlipper.findViewById(R.id.startTrainingButton);
        startTrainingButton.setOnClickListener(this::startTraining);

        // Add the Skeleton class as a listener for the Pose Data Manager
        poseDataManager.addPoseDataListener(binding.skeleton);
        poseDataManager.addPoseDataListener(binding.trainingManager);

        // Initialize Amplify for AWS S3 connection
        try {
            Amplify.addPlugin(new AndroidLoggingPlugin(LogLevel.VERBOSE));
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSS3StoragePlugin());
            Amplify.configure(getApplicationContext());
        } catch (AmplifyException e) {
            throw new RuntimeException(e);
        }

    }

    private void startTraining(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        binding.skeleton.setVisibility(View.INVISIBLE);
        binding.trainingManager.setVisibility(View.VISIBLE);
        binding.trainingManager.startTraining(fragmentManager);
        openCamera(view);

    }

    private void startExercising(View view) {
        binding.skeleton.setVisibility(View.VISIBLE);
        binding.trainingManager.setVisibility(View.INVISIBLE);
        openCamera(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clear all the Pose Data Listeners
        poseDataManager.clearPoseDataListeners();
    }

    public void openCamera(View listener) {
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(cameraLayout));
        if (cameraManager == null || cameraManager.isShutdown()) {
            cameraManager = new CameraManager(this, applicationContext, binding.camera, poseDataManager);
            cameraManager.start();
        }
    }

    public void openHomepage(View listener) {
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(homePage));
        cameraManager.stop();
        trainingManager.stopTraining();
    }
}