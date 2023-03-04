package csuci.seanhulse.fitness.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.room.Room;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import csuci.seanhulse.fitness.data.PoseDataManager;
import csuci.seanhulse.fitness.db.PoseDatabase;

/**
 * Image Analysis Analyzer which uses Google's Pose Detection ML API for detecting a human pose.
 *
 * @since 1.0.0
 */
public class Analyzer implements ImageAnalysis.Analyzer {

    private final PoseDetector poseDetector;
    private final boolean isImageFlipped;
    private final PoseDataManager poseDataManager;
    private final PoseDatabase db;

    public Analyzer(Context context, PoseDataManager poseDataManager, boolean isImageFlipped) {
        this.poseDataManager = poseDataManager;
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
                poseDataManager.setImageSourceInfo(imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
            } else {
                poseDataManager.setImageSourceInfo(imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
            }

            // Pass image to an ML Kit Vision API
            poseDetector.process(image)
                    .addOnSuccessListener(pose -> {
                                poseDataManager.addPose(pose);
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
