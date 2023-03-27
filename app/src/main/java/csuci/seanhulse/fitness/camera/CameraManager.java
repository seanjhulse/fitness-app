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

/**
 * Manages the camera authorization, processes, and aspect ratio.
 *
 * @since 1.0.0
 */
public class CameraManager {
    private final static int REQUEST_CODE_PERMISSIONS = 42;
    private final static String[] REQUIRED_PERMISSIONS = {permission.CAMERA};
    private final AppCompatActivity owner;
    private final Context context;
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private Analyzer analyzer;

    public CameraManager(AppCompatActivity owner, Context context) {
        this.owner = owner;
        this.context = context;
    }

    public void start(PreviewView previewView) {
        if (allPermissionsGranted()) {
            startCamera(previewView);
        } else {
            ActivityCompat.requestPermissions(owner, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    public void stop() {
        cameraExecutor.shutdown();
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    private void startCamera(PreviewView view) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {

            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            bindCameraUseCases();
            preview.setSurfaceProvider(view.getSurfaceProvider());
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
        preview = getPreviewUseCase(cameraSelector);
        UseCase poseDetector = createImageAnalysisUseCase();

        cameraProvider.unbindAll();

        cameraProvider.bindToLifecycle(owner, cameraSelector, preview, poseDetector);

    }

    private UseCase createImageAnalysisUseCase() {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);
        return imageAnalysis;
    }

    @SuppressLint("RestrictedApi")
    private Preview getPreviewUseCase(CameraSelector cameraSelector) {
        return new Preview.Builder()
                .setCameraSelector(cameraSelector)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build();
    }
}
