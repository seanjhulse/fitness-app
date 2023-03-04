package com.example.fitnessform;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessform.camera.CameraManager;
import com.example.fitnessform.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;

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

    }

    public void openCamera(View listener) {
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(cameraLayout));
        if (cameraManager == null) {
            cameraManager = new CameraManager(this, applicationContext, binding.camera, binding.skeleton);
            cameraManager.start();
        }
    }

    public void openHomepage(View listener) {
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(homePage));
        cameraManager.stop();
        cameraManager = null;
    }
}