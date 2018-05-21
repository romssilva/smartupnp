package com.romssilva.smartupnp.smartupnp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.fourthline.cling.model.meta.Device;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by romssilva on 2018-04-08.
 */

public class CameraTab extends Fragment {


    private static final String TAG = "CameraTab";

    private TextureView textureView;
    private TextView textView;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final int CAMERA_ID = -1;
    private static final boolean USE_FRONT_CAMERA = true;
    private static final boolean DEBUGGING = false;

    private String cameraId;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private CaptureRequest.Builder builder;
    private Size imageDimension;
    private Boolean surfaceTextureAvailable = false;

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private ImageClassifier classifier;
    private Boolean runClassifier = false;
    private final Object lock = new Object();

    private int classificationCount = 0;
    private final int CLASSIFICATION_COUNT_RATE = 4;
    private final int CLASSIFICATION_THRESHOLD = 100;

    private RecyclerView devicesList;
    private DeviceInSightAdapter deviceInSightAdapter;

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
        }
    };

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_tab, container, false);

        textureView = rootView.findViewById(R.id.textureView);
        textView = rootView.findViewById(R.id.textView);

        assert textureView != null;
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });

        devicesList = (RecyclerView) rootView.findViewById(R.id.devices_in_sight_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        devicesList.setLayoutManager(linearLayoutManager);

        deviceInSightAdapter = new DeviceInSightAdapter(rootView.getContext());
        devicesList.setAdapter(deviceInSightAdapter);

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreview() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            assert surfaceTexture != null;
            surfaceTexture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(surfaceTexture);
            builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null) {
                        return;
                    }

                    mCameraCaptureSession = cameraCaptureSession;
                    updatePreview();

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getActivity(), "Configure faile!", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updatePreview() {
        if (cameraDevice == null) {
            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            mCameraCaptureSession.setRepeatingRequest(builder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {

            if (CAMERA_ID >= 0) {
                cameraId = CAMERA_ID + "";
            }

            else if (USE_FRONT_CAMERA) {

                int cameraCount = cameraManager.getCameraIdList().length;
                for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraManager.getCameraIdList()[camIdx]);

                    if (cameraCharacteristics == null) {
                        throw new NullPointerException("No camera with id " + cameraId);
                    }
                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        cameraId = cameraManager.getCameraIdList()[camIdx];
                    }
                }

            } else {
                cameraId = cameraManager.getCameraIdList()[0];
            }

            if (cameraId == null) {
                Toast.makeText(getActivity(), "No front camera found!", Toast.LENGTH_SHORT).show();
                return;
            }

            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            if(ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }

            cameraManager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "You can't use camera without permission!", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG," ######### onResume #########");
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        synchronized (lock) {
            runClassifier = true;
        }
        mBackgroundHandler.post(periodicClassify);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onPause() {
        Log.i(TAG," ######### onPause #########");
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
                synchronized (lock) {
                    runClassifier = false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Takes photos and classify them periodically. */
    private Runnable periodicClassify =
        new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (runClassifier) {
                        classifyFrame();
                    }
                }
                mBackgroundHandler.post(periodicClassify);
            }
        };

    private void classifyFrame() {
        if (classifier == null || getActivity() == null || cameraDevice == null || textureView == null) {
            showToast("Uninitialized Classifier or invalid context.");
            return;
        }
        Bitmap bitmap = textureView.getBitmap(classifier.getImageSizeX(), classifier.getImageSizeY());
        String textToShow = classifier.classifyFrame(bitmap);

        //todo

        if (classifier.readyToGuess()) {
            Map.Entry<String, Float> mostLikelyClass = classifier.getMostLikelyClass();
            showToast(mostLikelyClass);

            StringBuilder sb = new StringBuilder();

            Collection<Device> devices = ((MainActivity) getActivity()).getDevicesList();
            if (devices != null) {
                for (Device device : devices) {
                    String[] words = mostLikelyClass.getKey().split(" ");
                    for (String word : words) {
//                        if (device.getDetails().getFriendlyName().toLowerCase().contains(word.toLowerCase())) {
                        if (true) {
                            addDevice(device);
                        }
                    }
                }
            }
        }

        if (bitmap != null) bitmap.recycle();
    }

    private void showToast(final Map.Entry<String, Float> entry) {
        if (DEBUGGING) return;
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                    textView.setText(String.format("%s: %4.2f", entry.getKey(), entry.getValue()));
                    }
                });
        }
    }

    private void showToast(final String text) {
        if (DEBUGGING) return;
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(text);
                        }
                    });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stop() {
        stopBackgroundThread();
        if (classifier != null) {
            classifier.close();
            classifier = null;
        }
        if (cameraDevice != null) cameraDevice.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void start() {

        try {
            // create either a new ImageClassifierQuantizedMobileNet or an ImageClassifierFloatInception
            if (classifier == null) classifier = new ImageClassifierQuantizedMobileNet(getActivity());
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize an image classifier.", e);
        }

        startBackgroundThread();

        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    public void addDevice(Device device) {
        deviceInSightAdapter.addDevice(device);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceInSightAdapter.notifyDataSetChanged();
            }
        });
    }
}
