package csuci.seanhulse.fitness;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.Amplify.AlreadyConfiguredException;
import com.amplifyframework.logging.AndroidLoggingPlugin;
import com.amplifyframework.logging.LogLevel;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import csuci.seanhulse.fitness.camera.CameraManager;
import csuci.seanhulse.fitness.data.PoseDataManager;
import csuci.seanhulse.fitness.databinding.ActivityMainBinding;
import csuci.seanhulse.fitness.training.TrainingFragment;
import csuci.seanhulse.fitness.training.TrainingManager;
import csuci.seanhulse.fitness.workouts.WorkoutsFragment;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private View cameraLayout;
    private View homePage;
    private TrainingManager trainingManager;
    private CameraManager cameraManager;
    private BottomNavigationView bottomNavigationView;
    private Context applicationContext;
    private ActivityMainBinding binding;
    private final PoseDataManager poseDataManager = new PoseDataManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        loadFragment(new HomeFragment());

//        this.cameraLayout = findViewById(R.id.cameraLayout);
//        this.homePage = findViewById(R.id.homepage);
//        this.trainingManager = findViewById(R.id.trainingManager);
//
//        MaterialButton defaultSquatButton = findViewById(R.id.defaultSquatButton);
//        defaultSquatButton.setOnClickListener(this::startExercising);
//
//        ImageButton openHomepageButton = findViewById(R.id.openHomepageButton);
//        openHomepageButton.setOnClickListener(this::openHomepage);
//
//        FloatingActionButton startTrainingButton = findViewById(R.id.startTrainingButton);
//        startTrainingButton.setOnClickListener(this::startTraining);

        // Add the Skeleton class as a listener for the Pose Data Manager
//        poseDataManager.addPoseDataListener(binding.skeleton);
//        poseDataManager.addPoseDataListener(binding.trainingManager);

        // Initialize Amplify for AWS S3 connection
        try {
            Amplify.addPlugin(new AndroidLoggingPlugin(LogLevel.VERBOSE));
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSS3StoragePlugin());
            Amplify.configure(getApplicationContext());
        } catch (AlreadyConfiguredException alreadyConfiguredException) {
            Log.d("Amplify", "Amplify is already configured");
        } catch (AmplifyException e) {
            throw new RuntimeException(e);
        }

    }

    private void startTraining(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
//        binding.skeleton.setVisibility(View.INVISIBLE);
//        binding.trainingManager.setVisibility(View.VISIBLE);
//        binding.trainingManager.startTraining(fragmentManager);
        openCamera(view);

    }

    private void startExercising(View view) {
//        binding.skeleton.setVisibility(View.VISIBLE);
//        binding.trainingManager.setVisibility(View.INVISIBLE);
        openCamera(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clear all the Pose Data Listeners
        poseDataManager.clearPoseDataListeners();
    }

    public void openCamera(View listener) {
//        if (cameraManager == null || cameraManager.isShutdown()) {
//            cameraManager = new CameraManager(this, applicationContext, binding.camera, poseDataManager);
//            cameraManager.start();
//        }
    }

    public void openHomepage(View listener) {
//        cameraManager.stop();
//        trainingManager.stopTraining();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.home:
                fragment = new HomeFragment();
                break;
            case R.id.workouts:
                fragment = new WorkoutsFragment();
                break;
            case R.id.training:
                fragment = new TrainingFragment();
                break;
        }
        if (fragment != null) {
            loadFragment(fragment);
        }
        return true;
    }

    void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.relativeLayout, fragment)
                .commit();
    }
}