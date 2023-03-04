package csuci.seanhulse.fitness.camera;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCase;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import csuci.seanhulse.fitness.data.PoseDataManager;

/**
 * Manages the camera authorization, processes, and aspect ratio.
 */
public class CameraManager {
    private final static int REQUEST_CODE_PERMISSIONS = 42;
    private final static String[] REQUIRED_PERMISSIONS = {permission.CAMERA};
    private final AppCompatActivity owner;
    private final PreviewView cameraView;
    private final Context context;
    private final Skeleton skeleton;
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private final PoseDataManager poseDataManager;

    private ProcessCameraProvider cameraProvider;

    public CameraManager(AppCompatActivity owner, Context context, PreviewView cameraView, Skeleton skeleton, PoseDataManager poseDataManager) {
        this.owner = owner;
        this.context = context;
        this.cameraView = cameraView;
        this.skeleton = skeleton;
        this.poseDataManager = poseDataManager;
    }

    public void start() {
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(owner, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    public void stop() {
        cameraExecutor.shutdown();
    }

    public boolean isShutdown() {
        return cameraExecutor.isShutdown();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {

            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            bindCameraUseCases();
        }, ContextCompat.getMainExecutor(context));
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void bindCameraUseCases() {
        int lensFacing = CameraSelector.LENS_FACING_FRONT;
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();
        Preview previewView = getPreviewUseCase(cameraSelector);
        UseCase poseDetector = createImageAnalysisUseCase();

        cameraProvider.unbindAll();

        cameraProvider.bindToLifecycle(owner, cameraSelector, previewView, poseDetector);

        previewView.setSurfaceProvider(cameraView.getSurfaceProvider());
    }

    private UseCase createImageAnalysisUseCase() {
        ImageAnalysis analyzer = new ImageAnalysis.Builder().build();
        analyzer.setAnalyzer(cameraExecutor, new Analyzer(skeleton, context, poseDataManager, true));
        return analyzer;
    }

    @SuppressLint("RestrictedApi")
    private Preview getPreviewUseCase(CameraSelector cameraSelector) {
        return new Preview.Builder()
                .setCameraSelector(cameraSelector)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build();
    }
}
