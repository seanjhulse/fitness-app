package csuci.seanhulse.fitness;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.Amplify.AlreadyConfiguredException;
import com.amplifyframework.logging.AndroidLoggingPlugin;
import com.amplifyframework.logging.LogLevel;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import csuci.seanhulse.fitness.camera.Analyzer;
import csuci.seanhulse.fitness.camera.CameraManager;
import csuci.seanhulse.fitness.data.PoseDataManager;
import csuci.seanhulse.fitness.home.HomeFragment;
import csuci.seanhulse.fitness.training.TrainingMenuFragment;
import csuci.seanhulse.fitness.workouts.WorkoutsFragment;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private final PoseDataManager poseDataManager = new PoseDataManager();
    private CameraManager cameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        cameraManager = new CameraManager(this, getApplicationContext());
        cameraManager.setAnalyzer(new Analyzer(poseDataManager, true));

        loadFragment(new HomeFragment());

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clear all the Pose Data Listeners
        poseDataManager.clearPoseDataListeners();
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
                fragment = new TrainingMenuFragment();
                break;
        }
        if (fragment != null) {
            loadFragment(fragment);
        }
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.relativeLayout, fragment)
                .commit();
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public PoseDataManager getPoseDataManager() {
        return poseDataManager;
    }
}