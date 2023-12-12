package com.google.android.cameraview;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import com.google.android.cameraview.PreviewImpl;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;


class Camera2 extends CameraViewImpl {
    private static final SparseIntArray INTERNAL_FACINGS;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final String TAG = "Camera2";
    private byte[] latestFrameData;
    private int latestFrameHeight;
    private int latestFrameWidth;
    private AspectRatio mAspectRatio;
    private boolean mAutoFocus;
    private boolean mAutoFocusSetting;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    CameraDevice mCamera;
    private CameraCharacteristics mCameraCharacteristics;
    private final CameraDevice.StateCallback mCameraDeviceCallback;
    private String mCameraId;
    private final CameraManager mCameraManager;
    PictureCaptureCallback mCaptureCallback;
    CameraCaptureSession mCaptureSession;
    private int mDisplayOrientation;
    private int mFacing;
    private int mFlash;
    private Handler mFrameHandler;
    private ImageReader mFrameImageReader;
    private Handler mFrameProcessHandler;
    private HandlerThread mFrameProcessThread;
    private HandlerThread mFrameThread;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnFrameAvailableListener;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener;
    private final SizeMap mPictureSizes;
    CaptureRequest.Builder mPreviewRequestBuilder;
    private final SizeMap mPreviewSizes;
    private final CameraCaptureSession.StateCallback mSessionCallback;
    protected Float mZoomDistance;
    protected Float maximumZoomLevel;
    protected Rect zoom;
    protected float zoomLevel;

    static {
        SparseIntArray sparseIntArray = new SparseIntArray();
        INTERNAL_FACINGS = sparseIntArray;
        sparseIntArray.put(0, 1);
        sparseIntArray.put(1, 0);
    }

    /* renamed from: com.google.android.cameraview.Camera2$5  reason: invalid class name */
    
    class AnonymousClass5 implements ImageReader.OnImageAvailableListener {
        AnonymousClass5() {
        }

        @Override // android.media.ImageReader.OnImageAvailableListener
        public void onImageAvailable(final ImageReader imageReader) {
            if (Camera2.this.mFrameHandler == null) {
                return;
            }
            Camera2.this.mFrameHandler.post(new Runnable() { // from class: com.google.android.cameraview.Camera2.5.1
                @Override 
                public void run() {
                    Image acquireNextImage = imageReader.acquireNextImage();
                    if (acquireNextImage == null) {
                        return;
                    }
                    try {
                        if (Camera2.this.onFrameCallback != null) {
                            Camera2.this.latestFrameData = Utils.YUV420toNV21(acquireNextImage);
                            Camera2.this.latestFrameWidth = acquireNextImage.getWidth();
                            Camera2.this.latestFrameHeight = acquireNextImage.getHeight();
                            Camera2.this.mFrameProcessHandler.post(new Runnable() { // from class: com.google.android.cameraview.Camera2.5.1.1
                                @Override 
                                public void run() {
                                    Camera2.this.onFrameCallback.onFrame(Camera2.this.latestFrameData, Camera2.this.latestFrameWidth, Camera2.this.latestFrameHeight, Camera2.this.getRotationDegrees());
                                }
                            });
                        }
                    } finally {
                        acquireNextImage.close();
                    }
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Camera2(PreviewImpl previewImpl, Context context) {
        super(previewImpl, context);
        this.mCameraDeviceCallback = new CameraDevice.StateCallback() { // from class: com.google.android.cameraview.Camera2.1
            @Override // android.hardware.camera2.CameraDevice.StateCallback
            public void onClosed(CameraDevice cameraDevice) {
            }

            @Override // android.hardware.camera2.CameraDevice.StateCallback
            public void onOpened(CameraDevice cameraDevice) {
                Camera2.this.mCamera = cameraDevice;
                Camera2.this.startCaptureSession();
            }

            @Override // android.hardware.camera2.CameraDevice.StateCallback
            public void onDisconnected(CameraDevice cameraDevice) {
                Camera2.this.mCamera = null;
            }

            @Override // android.hardware.camera2.CameraDevice.StateCallback
            public void onError(CameraDevice cameraDevice, int i) {
                Log.e(Camera2.TAG, "onError: " + cameraDevice.getId() + " (" + i + ")");
                Camera2.this.mCamera = null;
            }
        };
        this.mSessionCallback = new CameraCaptureSession.StateCallback() { // from class: com.google.android.cameraview.Camera2.2
            @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
            public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                if (Camera2.this.mCamera == null) {
                    return;
                }
                Camera2.this.mCaptureSession = cameraCaptureSession;
                Camera2.this.updateAutoFocus();
                Camera2.this.updateFlash();
                try {
                    Camera2.this.mCaptureSession.setRepeatingRequest(Camera2.this.mPreviewRequestBuilder.build(), Camera2.this.mCaptureCallback, Camera2.this.mBackgroundHandler);
                } catch (Exception e) {
                    if (Camera2.this.cameraErrorCallback != null) {
                        Camera2.this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera2.2.1
                            @Override 
                            public void run() {
                                Camera2.this.cameraErrorCallback.onCameraError(e);
                            }
                        });
                    }
                }
            }

            @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                Log.e(Camera2.TAG, "Failed to configure capture session.");
            }

            @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
            public void onClosed(CameraCaptureSession cameraCaptureSession) {
                if (Camera2.this.mCaptureSession == null || !Camera2.this.mCaptureSession.equals(cameraCaptureSession)) {
                    return;
                }
                Camera2.this.mCaptureSession = null;
            }
        };
        this.mCaptureCallback = new PictureCaptureCallback() { // from class: com.google.android.cameraview.Camera2.3
            @Override // com.google.android.cameraview.Camera2.PictureCaptureCallback
            public void onPrecaptureRequired() {
                Camera2.this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, 1);
                setState(3);
                try {
                    Camera2.this.mCaptureSession.capture(Camera2.this.mPreviewRequestBuilder.build(), this, Camera2.this.mBackgroundHandler);
                    Camera2.this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, 0);
                } catch (Exception e) {
                    if (Camera2.this.cameraErrorCallback != null) {
                        Camera2.this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera2.3.1
                            @Override 
                            public void run() {
                                Camera2.this.cameraErrorCallback.onCameraError(e);
                            }
                        });
                    }
                }
            }

            @Override // com.google.android.cameraview.Camera2.PictureCaptureCallback
            public void onReady() {
                Camera2.this.captureStillPicture();
            }
        };
        this.mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() { // from class: com.google.android.cameraview.Camera2.4
            @Override // android.media.ImageReader.OnImageAvailableListener
            public void onImageAvailable(ImageReader imageReader) {
                Image acquireNextImage = imageReader.acquireNextImage();
                try {
                    Image.Plane[] planes = acquireNextImage.getPlanes();
                    if (planes.length > 0) {
                        ByteBuffer buffer = planes[0].getBuffer();
                        byte[] bArr = new byte[buffer.capacity()];
                        buffer.get(bArr);
                        Camera2.this.byteArrayToBitmap(bArr);
                        if (Camera2.this.pictureBytesCallback != null) {
                            Camera2.this.pictureBytesCallback.onPictureBytesAvailable(bArr, Camera2.this.getRotationDegrees());
                        }
                    }
                    acquireNextImage.close();
                    if (acquireNextImage != null) {
                        acquireNextImage.close();
                    }
                } catch (Throwable th) {
                    try {
                        throw th;
                    } catch (Throwable th2) {
                        if (acquireNextImage != null) {
                            try {
                                acquireNextImage.close();
                            } catch (Throwable th3) {
                                th.addSuppressed(th3);
                            }
                        }
                        throw th2;
                    }
                }
            }
        };
        this.mOnFrameAvailableListener = new AnonymousClass5();
        this.mPreviewSizes = new SizeMap();
        this.mPictureSizes = new SizeMap();
        this.mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
        this.zoomLevel = 1.0f;
        this.mCameraManager = (CameraManager) context.getSystemService("camera");
        this.mPreview.setCallback(new PreviewImpl.Callback() { // from class: com.google.android.cameraview.Camera2.6
            @Override // com.google.android.cameraview.PreviewImpl.Callback
            public void onSurfaceChanged() {
                Camera2.this.startCaptureSession();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public boolean start() {
        this.orientation.startListening(this.orientationListener);
        if (chooseCameraIdByFacing()) {
            collectCameraInfo();
            prepareImageReader();
            startOpeningCamera();
            startBackgroundThread();
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void stop() {
        this.orientation.stopListening();
        CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            this.mCaptureSession = null;
        }
        CameraDevice cameraDevice = this.mCamera;
        if (cameraDevice != null) {
            cameraDevice.close();
            this.mCamera = null;
        }
        ImageReader imageReader = this.mImageReader;
        if (imageReader != null) {
            imageReader.close();
            this.mImageReader = null;
        }
        ImageReader imageReader2 = this.mFrameImageReader;
        if (imageReader2 != null) {
            imageReader2.close();
            this.mFrameImageReader = null;
        }
        stopBackgroundThread();
        resetZoom();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public boolean isCameraOpened() {
        return this.mCamera != null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void setFacing(int i) {
        if (this.mFacing == i) {
            return;
        }
        this.mFacing = i;
        if (isCameraOpened()) {
            stop();
            start();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public int getFacing() {
        return this.mFacing;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public Set<AspectRatio> getSupportedAspectRatios() {
        android.util.Size[] outputSizes;
        if (this.mPreviewSizes.isEmpty()) {
            chooseCameraIdByFacing();
            StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) this.mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap == null) {
                return this.mPreviewSizes.ratios();
            }
            for (android.util.Size size : streamConfigurationMap.getOutputSizes(this.mPreview.getOutputClass())) {
                int width = size.getWidth();
                int height = size.getHeight();
                if (width <= MAX_PREVIEW_WIDTH && height <= MAX_PREVIEW_HEIGHT) {
                    if (this.maximumPreviewWidth == 0) {
                        this.mPreviewSizes.add(new Size(width, height));
                    } else if (width <= this.maximumPreviewWidth && height <= this.maximumPreviewWidth) {
                        this.mPreviewSizes.add(new Size(width, height));
                    }
                }
            }
        }
        return this.mPreviewSizes.ratios();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public boolean setAspectRatio(AspectRatio aspectRatio, boolean z) {
        if (aspectRatio == null || aspectRatio.equals(this.mAspectRatio) || !getSupportedAspectRatios().contains(aspectRatio)) {
            return false;
        }
        this.mAspectRatio = aspectRatio;
        if (z) {
            return true;
        }
        prepareImageReader();
        CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            this.mCaptureSession = null;
            startCaptureSession();
            return true;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public AspectRatio getAspectRatio() {
        return this.mAspectRatio;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void setAutoFocus(boolean z) {
        if (this.mAutoFocus == z) {
            return;
        }
        this.mAutoFocus = z;
        this.mAutoFocusSetting = z;
        if (this.mPreviewRequestBuilder != null) {
            updateAutoFocus();
            CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
            if (cameraCaptureSession != null) {
                try {
                    cameraCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, this.mBackgroundHandler);
                } catch (CameraAccessException unused) {
                    this.mAutoFocus = !this.mAutoFocus;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public boolean getAutoFocus() {
        return this.mAutoFocusSetting;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void setFlash(int i) {
        int i2 = this.mFlash;
        if (i2 == i) {
            return;
        }
        this.mFlash = i;
        if (this.mPreviewRequestBuilder != null) {
            updateFlash();
            CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
            if (cameraCaptureSession != null) {
                try {
                    cameraCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, this.mBackgroundHandler);
                } catch (CameraAccessException unused) {
                    this.mFlash = i2;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public int getFlash() {
        return this.mFlash;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void takePicture() {
        if (this.mAutoFocus) {
            lockFocus();
        } else {
            captureStillPicture();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void setDisplayOrientation(int i) {
        this.mDisplayOrientation = i;
        this.mPreview.setDisplayOrientation(this.mDisplayOrientation);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public int getCameraDefaultOrientation() {
        try {
            if (getFacing() == 1) {
                return ((Integer) this.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue() - 180;
            }
            return ((Integer) this.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
        } catch (Exception unused) {
            Log.w(TAG, "Failed to get CameraDefaultOrientation");
            return 0;
        }
    }

    private boolean chooseCameraIdByFacing() {
        try {
            int i = INTERNAL_FACINGS.get(this.mFacing);
            String[] cameraIdList = this.mCameraManager.getCameraIdList();
            if (cameraIdList.length == 0) {
                throw new RuntimeException("No camera available.");
            }
            for (String str : cameraIdList) {
                CameraCharacteristics cameraCharacteristics = this.mCameraManager.getCameraCharacteristics(str);
                Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                if (num != null && num.intValue() != 2) {
                    Integer num2 = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (num2 == null) {
                        throw new NullPointerException("Unexpected state: LENS_FACING null");
                    }
                    if (num2.intValue() == i) {
                        this.mCameraId = str;
                        this.mCameraCharacteristics = cameraCharacteristics;
                        return true;
                    }
                }
            }
            String str2 = cameraIdList[0];
            this.mCameraId = str2;
            CameraCharacteristics cameraCharacteristics2 = this.mCameraManager.getCameraCharacteristics(str2);
            this.mCameraCharacteristics = cameraCharacteristics2;
            Integer num3 = (Integer) cameraCharacteristics2.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            if (num3 != null && num3.intValue() != 2) {
                Integer num4 = (Integer) this.mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (num4 == null) {
                    throw new NullPointerException("Unexpected state: LENS_FACING null");
                }
                int size = INTERNAL_FACINGS.size();
                for (int i2 = 0; i2 < size; i2++) {
                    SparseIntArray sparseIntArray = INTERNAL_FACINGS;
                    if (sparseIntArray.valueAt(i2) == num4.intValue()) {
                        this.mFacing = sparseIntArray.keyAt(i2);
                        return true;
                    }
                }
                if (this.turnFailCallback != null) {
                    this.turnFailCallback.onTurnCameraFail(new RuntimeException("Cannot find suitable Camera"));
                }
                this.mFacing = 0;
                return true;
            }
            return false;
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to get a list of camera devices", e);
        }
    }

    private void collectCameraInfo() {
        android.util.Size[] outputSizes;
        StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) this.mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (streamConfigurationMap == null) {
            throw new IllegalStateException("Failed to get configuration map: " + this.mCameraId);
        }
        this.mPreviewSizes.clear();
        for (android.util.Size size : streamConfigurationMap.getOutputSizes(this.mPreview.getOutputClass())) {
            int width = size.getWidth();
            int height = size.getHeight();
            if (width <= MAX_PREVIEW_WIDTH && height <= MAX_PREVIEW_HEIGHT) {
                if (this.maximumPreviewWidth == 0) {
                    this.mPreviewSizes.add(new Size(width, height));
                } else if (width <= this.maximumPreviewWidth && height <= this.maximumPreviewWidth) {
                    this.mPreviewSizes.add(new Size(width, height));
                }
            }
        }
        this.mPictureSizes.clear();
        collectPictureSizes(this.mPictureSizes, streamConfigurationMap);
        for (AspectRatio aspectRatio : this.mPreviewSizes.ratios()) {
            if (!this.mPictureSizes.ratios().contains(aspectRatio)) {
                this.mPreviewSizes.remove(aspectRatio);
            }
        }
        if (this.mPreviewSizes.ratios().contains(this.mAspectRatio)) {
            return;
        }
        this.mAspectRatio = this.mPreviewSizes.ratios().iterator().next();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void collectPictureSizes(SizeMap sizeMap, StreamConfigurationMap streamConfigurationMap) {
        android.util.Size[] outputSizes;
        for (android.util.Size size : streamConfigurationMap.getOutputSizes(256)) {
            Log.i("CameraView2", "Picture Size: " + size.toString());
            if (this.maximumWidth == 0) {
                this.mPictureSizes.add(new Size(size.getWidth(), size.getHeight()));
            } else if (size.getWidth() <= this.maximumWidth && size.getHeight() <= this.maximumWidth) {
                this.mPictureSizes.add(new Size(size.getWidth(), size.getHeight()));
            }
        }
    }

    private void prepareImageReader() {
        ImageReader imageReader = this.mImageReader;
        if (imageReader != null) {
            imageReader.close();
        }
        ImageReader imageReader2 = this.mFrameImageReader;
        if (imageReader2 != null) {
            imageReader2.close();
        }
        this.mPictureSizeSelected = this.mPictureSizes.sizes(this.mAspectRatio).last();
        this.mImageReader = ImageReader.newInstance(this.mPictureSizeSelected.getWidth(), this.mPictureSizeSelected.getHeight(), 256, 1);
        Size last = this.mPreviewSizes.sizes(this.mAspectRatio).last();
        this.mFrameImageReader = ImageReader.newInstance(last.getWidth(), last.getHeight(), 35, 1);
        this.mImageReader.setOnImageAvailableListener(this.mOnImageAvailableListener, this.mBackgroundHandler);
        this.mFrameImageReader.setOnImageAvailableListener(this.mOnFrameAvailableListener, this.mBackgroundHandler);
    }

    private void startOpeningCamera() {
        try {
            this.mCameraManager.openCamera(this.mCameraId, this.mCameraDeviceCallback, this.mBackgroundHandler);
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera2.7
                    @Override 
                    public void run() {
                        Camera2.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
    }

    void startCaptureSession() {
        if (!isCameraOpened() || !this.mPreview.isReady() || this.mImageReader == null || this.mFrameImageReader == null) {
            return;
        }
        this.mPreviewSizeSelected = chooseOptimalSize();
        this.mPreview.setBufferSize(this.mPreviewSizeSelected.getWidth(), this.mPreviewSizeSelected.getHeight());
        Surface surface = this.mPreview.getSurface();
        Surface surface2 = this.mFrameImageReader.getSurface();
        try {
            CaptureRequest.Builder createCaptureRequest = this.mCamera.createCaptureRequest(1);
            this.mPreviewRequestBuilder = createCaptureRequest;
            createCaptureRequest.addTarget(surface);
            this.mPreviewRequestBuilder.addTarget(surface2);
            this.mCamera.createCaptureSession(Arrays.asList(surface, this.mImageReader.getSurface(), this.mFrameImageReader.getSurface()), this.mSessionCallback, this.mBackgroundHandler);
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera2.8
                    @Override 
                    public void run() {
                        Camera2.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
    }

    private Size chooseOptimalSize() {
        int width = this.mPreview.getWidth();
        int height = this.mPreview.getHeight();
        if (width < height) {
            height = width;
            width = height;
        }
        SortedSet<Size> sizes = this.mPreviewSizes.sizes(this.mAspectRatio);
        for (Size size : sizes) {
            if (size.getWidth() >= width && size.getHeight() >= height) {
                return size;
            }
        }
        return sizes.last();
    }

    void updateAutoFocus() {
        if (this.mAutoFocusSetting) {
            int[] iArr = (int[]) this.mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            if (iArr == null || iArr.length == 0 || (iArr.length == 1 && iArr[0] == 0)) {
                this.mAutoFocus = false;
                this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, 0);
                return;
            }
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, 4);
            return;
        }
        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, 0);
    }

    void updateFlash() {
        int i = this.mFlash;
        if (i == 0) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 1);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 0);
        } else if (i == 1) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 3);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 0);
        } else if (i == 2) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 1);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 2);
        } else if (i == 3) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 2);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 0);
        } else if (i != 4) {
        } else {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, 4);
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, 0);
        }
    }

    private void lockFocus() {
        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, 1);
        try {
            this.mCaptureCallback.setState(1);
            this.mCaptureSession.capture(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, this.mBackgroundHandler);
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera2.9
                    @Override 
                    public void run() {
                        Camera2.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
    }

    void captureStillPicture() {
        try {
            CaptureRequest.Builder createCaptureRequest = this.mCamera.createCaptureRequest(2);
            createCaptureRequest.addTarget(this.mImageReader.getSurface());
            createCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, this.mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_MODE));
            if (this.zoom != null) {
                createCaptureRequest.set(CaptureRequest.SCALER_CROP_REGION, this.zoom);
            }
            int i = this.mFlash;
            if (i == 0) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 1);
                createCaptureRequest.set(CaptureRequest.FLASH_MODE, 0);
            } else if (i == 1) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 3);
            } else if (i == 2) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 1);
                createCaptureRequest.set(CaptureRequest.FLASH_MODE, 2);
            } else if (i == 3) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 2);
            } else if (i == 4) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, 2);
            }
            this.mCaptureSession.stopRepeating();
            if (this.focusLockedCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera2.10
                    @Override 
                    public void run() {
                        Camera2.this.focusLockedCallback.onFocusLocked();
                    }
                });
            }
            this.mCaptureSession.capture(createCaptureRequest.build(), new CameraCaptureSession.CaptureCallback() { // from class: com.google.android.cameraview.Camera2.11
                @Override // android.hardware.camera2.CameraCaptureSession.CaptureCallback
                public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
                    Camera2.this.unlockFocus();
                }
            }, this.mBackgroundHandler);
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera2.12
                    @Override 
                    public void run() {
                        Camera2.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
    }

    void unlockFocus() {
        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, 2);
        try {
            this.mCaptureSession.capture(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, this.mBackgroundHandler);
            updateAutoFocus();
            updateFlash();
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, 0);
            this.mCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, this.mBackgroundHandler);
            this.mCaptureCallback.setState(0);
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera2.13
                    @Override 
                    public void run() {
                        Camera2.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
    }

    private void startBackgroundThread() {
        HandlerThread handlerThread = new HandlerThread("CameraBackground");
        this.mBackgroundThread = handlerThread;
        handlerThread.start();
        this.mBackgroundHandler = new Handler(this.mBackgroundThread.getLooper());
        HandlerThread handlerThread2 = new HandlerThread("CameraFrameBackground");
        this.mFrameThread = handlerThread2;
        handlerThread2.start();
        this.mFrameHandler = new Handler(this.mFrameThread.getLooper());
        HandlerThread handlerThread3 = new HandlerThread("CameraFrameProcessBackground");
        this.mFrameProcessThread = handlerThread3;
        handlerThread3.start();
        this.mFrameProcessHandler = new Handler(this.mFrameProcessThread.getLooper());
    }

    private void stopBackgroundThread() {
        try {
            HandlerThread handlerThread = this.mBackgroundThread;
            if (handlerThread != null) {
                handlerThread.quitSafely();
            }
            HandlerThread handlerThread2 = this.mBackgroundThread;
            if (handlerThread2 != null) {
                handlerThread2.join();
            }
            this.mBackgroundThread = null;
            this.mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            HandlerThread handlerThread3 = this.mFrameThread;
            if (handlerThread3 != null) {
                handlerThread3.quit();
            }
            HandlerThread handlerThread4 = this.mFrameThread;
            if (handlerThread4 != null) {
                handlerThread4.join();
            }
            this.mFrameThread = null;
            this.mFrameHandler = null;
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        try {
            HandlerThread handlerThread5 = this.mFrameProcessThread;
            if (handlerThread5 != null) {
                handlerThread5.quit();
            }
            HandlerThread handlerThread6 = this.mFrameProcessThread;
            if (handlerThread6 != null) {
                handlerThread6.join();
            }
            this.mFrameProcessThread = null;
            this.mFrameProcessHandler = null;
        } catch (InterruptedException e3) {
            e3.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public boolean zoom(MotionEvent motionEvent) {
        float floatValue;
        boolean z;
        try {
            Rect rect = (Rect) this.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (rect == null) {
                return false;
            }
            if (this.maximumZoomLevel == null) {
                this.maximumZoomLevel = (Float) this.mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            }
            float fingerSpacing = getFingerSpacing(motionEvent);
            Float f = this.mZoomDistance;
            if (f == null) {
                this.mZoomDistance = Float.valueOf(fingerSpacing);
                return true;
            }
            if (fingerSpacing >= f.floatValue()) {
                floatValue = fingerSpacing - this.mZoomDistance.floatValue();
                z = true;
            } else {
                floatValue = this.mZoomDistance.floatValue() - fingerSpacing;
                z = false;
            }
            float f2 = floatValue / this.pixelsPerOneZoomLevel;
            if (z && this.zoomLevel + f2 > this.maximumZoomLevel.floatValue()) {
                f2 = this.maximumZoomLevel.floatValue() - this.zoomLevel;
            } else if (!z) {
                float f3 = this.zoomLevel;
                if (f3 - f2 < 1.0f) {
                    f2 = f3 - 1.0f;
                }
            }
            if (z) {
                this.zoomLevel += f2;
            } else {
                this.zoomLevel -= f2;
            }
            float f4 = 1.0f / this.zoomLevel;
            int width = rect.width() - Math.round(rect.width() * f4);
            int height = rect.height() - Math.round(rect.height() * f4);
            this.zoom = new Rect(width / 2, height / 2, rect.width() - (width / 2), rect.height() - (height / 2));
            this.mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, this.zoom);
            this.mCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, this.mBackgroundHandler);
            this.mZoomDistance = Float.valueOf(fingerSpacing);
            return true;
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera2.14
                    @Override 
                    public void run() {
                        Camera2.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void onPinchFingerUp() {
        this.mZoomDistance = null;
    }

    void resetZoom() {
        this.zoomLevel = 1.0f;
        this.zoom = null;
        this.mZoomDistance = Float.valueOf(0.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    
    public static abstract class PictureCaptureCallback extends CameraCaptureSession.CaptureCallback {
        static final int STATE_CAPTURING = 5;
        static final int STATE_LOCKED = 2;
        static final int STATE_LOCKING = 1;
        static final int STATE_PRECAPTURE = 3;
        static final int STATE_PREVIEW = 0;
        static final int STATE_WAITING = 4;
        private int mState;

        public abstract void onPrecaptureRequired();

        public abstract void onReady();

        PictureCaptureCallback() {
        }

        void setState(int i) {
            this.mState = i;
        }

        @Override // android.hardware.camera2.CameraCaptureSession.CaptureCallback
        public void onCaptureProgressed(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, CaptureResult captureResult) {
            process(captureResult);
        }

        @Override // android.hardware.camera2.CameraCaptureSession.CaptureCallback
        public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {
            process(totalCaptureResult);
        }

        private void process(CaptureResult captureResult) {
            int i = this.mState;
            if (i == 1) {
                Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                if (num == null) {
                    return;
                }
                if (num.intValue() == 4 || num.intValue() == 5) {
                    Integer num2 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                    if (num2 == null || num2.intValue() == 2) {
                        setState(5);
                        onReady();
                        return;
                    }
                    setState(2);
                    onPrecaptureRequired();
                }
            } else if (i == 3) {
                Integer num3 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                if (num3 == null || num3.intValue() == 5 || num3.intValue() == 4 || num3.intValue() == 2) {
                    setState(4);
                }
            } else if (i != 4) {
            } else {
                Integer num4 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                if (num4 == null || num4.intValue() != 5) {
                    setState(5);
                    onReady();
                }
            }
        }
    }
}
