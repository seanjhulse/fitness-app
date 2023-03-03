package com.example.fitnessform.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.room.Room;

import com.example.fitnessform.db.PoseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

public class Analyzer implements ImageAnalysis.Analyzer {

    private final Skeleton skeleton;
    private final PoseDetector poseDetector;
    private final boolean isImageFlipped;
    private final PoseDatabase db;

    public Analyzer(Skeleton skeleton, Context context, boolean isImageFlipped) {
        this.skeleton = skeleton;
        this.isImageFlipped = isImageFlipped;

        AccuratePoseDetectorOptions poseDetectorOptions = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build();

        poseDetector = PoseDetection.getClient(poseDetectorOptions);

        this.db = Room
                .databaseBuilder(context, PoseDatabase.class, "pose-database")
                .build();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            InputImage image = InputImage.fromMediaImage(mediaImage, rotationDegrees);

            if (rotationDegrees == 0 || rotationDegrees == 180) {
                skeleton.setImageSourceInfo(imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
            } else {
                skeleton.setImageSourceInfo(imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
            }

            // Pass image to an ML Kit Vision API
            poseDetector.process(image)
                    .addOnSuccessListener(pose -> {
                                PoseGraphic graphic = new PoseGraphic(skeleton, pose,
                                        true,
                                        true,
                                        true,
                                        pose.getAllPoseLandmarks());

                                skeleton.clear();
                                skeleton.add(graphic);
                                skeleton.postInvalidate();
                                try {
                                    imageProxy.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    )
                    .addOnFailureListener(Throwable::printStackTrace);
        }
    }

}
