package csuci.seanhulse.fitness;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import csuci.seanhulse.fitness.camera.CameraManager;
import com.example.fitness.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private ViewFlipper viewFlipper;
    private View cameraLayout;
    private View homePage;
    private CameraManager cameraManager;
    private Context applicationContext;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        this.applicationContext = this.getApplicationContext();
        this.viewFlipper = findViewById(R.id.viewFlipper);
        this.cameraLayout = viewFlipper.findViewById(R.id.cameraLayout);
        this.homePage = viewFlipper.findViewById(R.id.homepage);

        MaterialButton defaultSquatButton = viewFlipper.findViewById(R.id.defaultSquatButton);
        defaultSquatButton.setOnClickListener(this::openCamera);

        ImageButton openHomepageButton = viewFlipper.findViewById(R.id.openHomepageButton);
        openHomepageButton.setOnClickListener(this::openHomepage);

        FloatingActionButton trainWorkoutButton = viewFlipper.findViewById(R.id.trainWorkoutButton);
        trainWorkoutButton.setOnClickListener(this::openCamera);

    }

    public void openCamera(View listener) {
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(cameraLayout));
        if (cameraManager == null || cameraManager.isShutdown()) {
            cameraManager = new CameraManager(this, applicationContext, binding.camera, binding.skeleton);
            cameraManager.start();
        }
    }

    public void openHomepage(View listener) {
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(homePage));
        cameraManager.stop();
    }
}