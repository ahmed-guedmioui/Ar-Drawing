package com.google.android.cameraview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import androidx.collection.SparseArrayCompat;
import com.google.android.cameraview.PreviewImpl;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlinx.coroutines.DebugKt;


class Camera1 extends CameraViewImpl {
    private static final SparseArrayCompat<String> FLASH_MODES;
    private static final int INVALID_CAMERA_ID = -1;
    private final AtomicBoolean isPictureCaptureInProgress;
    private byte[] latestFrameData;
    private int latestFrameHeight;
    private int latestFrameWidth;
    private AspectRatio mAspectRatio;
    private boolean mAutoFocus;
    private Camera mCamera;
    private int mCameraId;
    private final Camera.CameraInfo mCameraInfo;
    private Camera.Parameters mCameraParameters;
    private int mDisplayOrientation;
    private int mFacing;
    private int mFlash;
    private Handler mFrameHandler;
    private HandlerThread mFrameThread;
    private final SizeMap mPictureSizes;
    private final SizeMap mPreviewSizes;
    private boolean mShowingPreview;
    protected Float mZoomDistance;

    private boolean isLandscape(int i) {
        return i == 90 || i == 270;
    }

    static {
        SparseArrayCompat<String> sparseArrayCompat = new SparseArrayCompat<>();
        FLASH_MODES = sparseArrayCompat;
        sparseArrayCompat.put(0, DebugKt.DEBUG_PROPERTY_VALUE_OFF);
        sparseArrayCompat.put(1, DebugKt.DEBUG_PROPERTY_VALUE_ON);
        sparseArrayCompat.put(2, "torch");
        sparseArrayCompat.put(3, DebugKt.DEBUG_PROPERTY_VALUE_AUTO);
        sparseArrayCompat.put(4, "red-eye");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Camera1(PreviewImpl previewImpl, Context context) {
        super(previewImpl, context);
        this.isPictureCaptureInProgress = new AtomicBoolean(false);
        this.mCameraInfo = new Camera.CameraInfo();
        this.mPreviewSizes = new SizeMap();
        this.mPictureSizes = new SizeMap();
        previewImpl.setCallback(new PreviewImpl.Callback() { // from class: com.google.android.cameraview.Camera1.1
            @Override // com.google.android.cameraview.PreviewImpl.Callback
            public void onSurfaceChanged() {
                if (Camera1.this.mCamera != null) {
                    Camera1.this.setUpPreview();
                    Camera1.this.adjustCameraParameters();
                    Camera1.this.setupPreviewCallback();
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public boolean start() {
        this.orientation.startListening(this.orientationListener);
        chooseCamera();
        openCamera();
        if (this.mPreview.isReady()) {
            setUpPreview();
            setupPreviewCallback();
        }
        this.mShowingPreview = true;
        startBackgroundThread();
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.startPreview();
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void stop() {
        this.orientation.stopListening();
        this.latestFrameWidth = 0;
        this.latestFrameHeight = 0;
        stopBackgroundThread();
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.stopPreview();
        }
        this.mShowingPreview = false;
        releaseCamera();
    }

    void setUpPreview() {
        try {
            if (this.mPreview.getOutputClass() == SurfaceHolder.class) {
                this.mCamera.setPreviewDisplay(this.mPreview.getSurfaceHolder());
            } else {
                this.mCamera.setPreviewTexture((SurfaceTexture) this.mPreview.getSurfaceTexture());
            }
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera1.2
                    @Override 
                    public void run() {
                        Camera1.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
    }

    void setupPreviewCallback() {
        try {
            this.mCamera.setPreviewCallback(new Camera.PreviewCallback() { // from class: com.google.android.cameraview.Camera1.3
                @Override // android.hardware.Camera.PreviewCallback
                public void onPreviewFrame(byte[] bArr, Camera camera) {
                    if (bArr == null || Camera1.this.isPictureCaptureInProgress.get() || Camera1.this.mFrameHandler == null || Camera1.this.onFrameCallback == null) {
                        return;
                    }
                    Camera1.this.latestFrameData = bArr;
                    if (Camera1.this.latestFrameWidth == 0) {
                        Camera1.this.latestFrameWidth = camera.getParameters().getPreviewSize().width;
                    }
                    if (Camera1.this.latestFrameHeight == 0) {
                        Camera1.this.latestFrameHeight = camera.getParameters().getPreviewSize().height;
                    }
                    Camera1.this.mFrameHandler.post(new Runnable() { // from class: com.google.android.cameraview.Camera1.3.1
                        @Override 
                        public void run() {
                            Camera1.this.onFrameCallback.onFrame(Camera1.this.latestFrameData, Camera1.this.latestFrameWidth, Camera1.this.latestFrameHeight, Camera1.this.getRotationDegrees());
                        }
                    });
                }
            });
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera1.4
                    @Override 
                    public void run() {
                        Camera1.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
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
        SizeMap sizeMap = this.mPreviewSizes;
        for (AspectRatio aspectRatio : sizeMap.ratios()) {
            if (this.mPictureSizes.sizes(aspectRatio) == null) {
                sizeMap.remove(aspectRatio);
            }
        }
        return sizeMap.ratios();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public boolean setAspectRatio(AspectRatio aspectRatio, boolean z) {
        if (this.mAspectRatio == null || !isCameraOpened()) {
            this.mAspectRatio = aspectRatio;
            return true;
        } else if (this.mAspectRatio.equals(aspectRatio)) {
            return false;
        } else {
            if (this.mPreviewSizes.sizes(aspectRatio) == null) {
                throw new UnsupportedOperationException(aspectRatio + " is not supported");
            }
            this.mAspectRatio = aspectRatio;
            adjustCameraParameters();
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public AspectRatio getAspectRatio() {
        return this.mAspectRatio;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void setAutoFocus(boolean z) {
        if (this.mAutoFocus != z && setAutoFocusInternal(z)) {
            this.mCamera.setParameters(this.mCameraParameters);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public boolean getAutoFocus() {
        if (!isCameraOpened()) {
            return this.mAutoFocus;
        }
        String focusMode = this.mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void setFlash(int i) {
        if (i != this.mFlash && setFlashInternal(i)) {
            this.mCamera.setParameters(this.mCameraParameters);
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
        if (!isCameraOpened()) {
            throw new IllegalStateException("Camera is not ready. Call start() before takePicture().");
        }
        if (getAutoFocus()) {
            this.mCamera.cancelAutoFocus();
            this.mCamera.autoFocus(new Camera.AutoFocusCallback() { // from class: com.google.android.cameraview.Camera1.5
                @Override // android.hardware.Camera.AutoFocusCallback
                public void onAutoFocus(boolean z, Camera camera) {
                    Camera1.this.takePictureInternal();
                }
            });
            return;
        }
        takePictureInternal();
    }

    void takePictureInternal() {
        stopBackgroundThread();
        try {
            if (this.isPictureCaptureInProgress.getAndSet(true)) {
                return;
            }
            this.mCamera.takePicture(new Camera.ShutterCallback() { // from class: com.google.android.cameraview.Camera1.6
                @Override // android.hardware.Camera.ShutterCallback
                public void onShutter() {
                    if (Camera1.this.focusLockedCallback != null) {
                        Camera1.this.focusLockedCallback.onFocusLocked();
                    }
                }
            }, null, null, new Camera.PictureCallback() { // from class: com.google.android.cameraview.Camera1.7
                @Override // android.hardware.Camera.PictureCallback
                public void onPictureTaken(byte[] bArr, Camera camera) {
                    Camera1.this.isPictureCaptureInProgress.set(false);
                    if (Camera1.this.pictureBytesCallback != null) {
                        Camera1.this.pictureBytesCallback.onPictureBytesAvailable(bArr, Camera1.this.getRotationDegrees());
                    }
                    Camera1.this.byteArrayToBitmap(bArr);
                    if (camera != null) {
                        camera.cancelAutoFocus();
                        camera.startPreview();
                    }
                    Camera1.this.startBackgroundThread();
                }
            });
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera1.8
                    @Override 
                    public void run() {
                        Camera1.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
            startBackgroundThread();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public void setDisplayOrientation(int i) {
        try {
            if (this.mDisplayOrientation == i) {
                return;
            }
            this.mDisplayOrientation = i;
            if (isCameraOpened()) {
                this.mCameraParameters.setRotation(i);
                this.mCamera.setParameters(this.mCameraParameters);
            }
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera1.9
                    @Override 
                    public void run() {
                        Camera1.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.cameraview.CameraViewImpl
    public int getCameraDefaultOrientation() {
        if (this.mCameraInfo != null) {
            return getFacing() == 1 ? this.mCameraInfo.orientation - 180 : this.mCameraInfo.orientation;
        }
        return 0;
    }

    private void chooseCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, this.mCameraInfo);
            if (this.mCameraInfo.facing == this.mFacing) {
                this.mCameraId = i;
                return;
            }
        }
        this.mCameraId = -1;
        if (this.turnFailCallback != null) {
            this.turnFailCallback.onTurnCameraFail(new RuntimeException("Cannot find suitable camera."));
        }
    }

    private void openCamera() {
        try {
            if (this.mCamera != null) {
                releaseCamera();
            }
            Camera open = Camera.open(this.mCameraId);
            this.mCamera = open;
            this.mCameraParameters = open.getParameters();
            this.mPreviewSizes.clear();
            for (Camera.Size size : this.mCameraParameters.getSupportedPreviewSizes()) {
                if (this.maximumPreviewWidth == 0) {
                    this.mPreviewSizes.add(new Size(size.width, size.height));
                } else if (size.width <= this.maximumPreviewWidth && size.height <= this.maximumPreviewWidth) {
                    this.mPreviewSizes.add(new Size(size.width, size.height));
                }
            }
            this.mPictureSizes.clear();
            for (Camera.Size size2 : this.mCameraParameters.getSupportedPictureSizes()) {
                Log.i("CameraView2", "Picture Size: " + size2.toString());
                if (this.maximumWidth == 0) {
                    this.mPictureSizes.add(new Size(size2.width, size2.height));
                } else if (size2.width <= this.maximumWidth && size2.height <= this.maximumWidth) {
                    this.mPictureSizes.add(new Size(size2.width, size2.height));
                }
            }
            if (this.mAspectRatio == null) {
                this.mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
            }
            adjustCameraParameters();
            this.mCamera.setDisplayOrientation(calcDisplayOrientation(this.mDisplayOrientation));
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera1.10
                    @Override 
                    public void run() {
                        Camera1.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
    }

    private AspectRatio chooseAspectRatio() {
        Iterator<AspectRatio> it = this.mPreviewSizes.ratios().iterator();
        AspectRatio aspectRatio = null;
        while (it.hasNext()) {
            aspectRatio = it.next();
            if (aspectRatio.equals(Constants.DEFAULT_ASPECT_RATIO)) {
                break;
            }
        }
        return aspectRatio;
    }

    void adjustCameraParameters() {
        Camera camera;
        try {
            SortedSet<Size> sizes = this.mPreviewSizes.sizes(this.mAspectRatio);
            if (sizes == null) {
                AspectRatio chooseAspectRatio = chooseAspectRatio();
                this.mAspectRatio = chooseAspectRatio;
                sizes = this.mPreviewSizes.sizes(chooseAspectRatio);
            }
            this.mPreviewSizeSelected = chooseOptimalSize(sizes);
            this.mPictureSizeSelected = this.mPictureSizes.sizes(this.mAspectRatio).last();
            if (this.mShowingPreview) {
                this.mCamera.stopPreview();
            }
            this.mCameraParameters.setPreviewSize(this.mPreviewSizeSelected.getWidth(), this.mPreviewSizeSelected.getHeight());
            this.mCameraParameters.setPictureSize(this.mPictureSizeSelected.getWidth(), this.mPictureSizeSelected.getHeight());
            this.mCameraParameters.setRotation(this.mDisplayOrientation);
            setAutoFocusInternal(this.mAutoFocus);
            setFlashInternal(this.mFlash);
            this.mCamera.setParameters(this.mCameraParameters);
            if (!this.mShowingPreview || (camera = this.mCamera) == null) {
                return;
            }
            camera.startPreview();
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera1.11
                    @Override 
                    public void run() {
                        Camera1.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
        }
    }

    private Size chooseOptimalSize(SortedSet<Size> sortedSet) {
        if (!this.mPreview.isReady()) {
            return sortedSet.first();
        }
        int width = this.mPreview.getWidth();
        int height = this.mPreview.getHeight();
        if (isLandscape(this.mDisplayOrientation)) {
            height = width;
            width = height;
        }
        Size size = null;
        Iterator<Size> it = sortedSet.iterator();
        while (it.hasNext()) {
            size = it.next();
            if (width <= size.getWidth() && height <= size.getHeight()) {
                break;
            }
        }
        return size;
    }

    private void releaseCamera() {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.setPreviewCallback(null);
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    private int calcDisplayOrientation(int i) {
        if (this.mCameraInfo.facing == 1) {
            return (360 - ((this.mCameraInfo.orientation + i) % 360)) % 360;
        }
        return ((this.mCameraInfo.orientation - i) + 360) % 360;
    }

    private int calcCameraRotation(int i) {
        if (this.mCameraInfo.facing == 1) {
            return (this.mCameraInfo.orientation + i) % 360;
        }
        return ((this.mCameraInfo.orientation + i) + (isLandscape(i) ? 180 : 0)) % 360;
    }

    private boolean setAutoFocusInternal(boolean z) {
        try {
            this.mAutoFocus = z;
            if (isCameraOpened()) {
                List<String> supportedFocusModes = this.mCameraParameters.getSupportedFocusModes();
                if (z && supportedFocusModes.contains("continuous-picture")) {
                    this.mCameraParameters.setFocusMode("continuous-picture");
                    return true;
                } else if (supportedFocusModes.contains("fixed")) {
                    this.mCameraParameters.setFocusMode("fixed");
                    return true;
                } else if (supportedFocusModes.contains("infinity")) {
                    this.mCameraParameters.setFocusMode("infinity");
                    return true;
                } else {
                    this.mCameraParameters.setFocusMode(supportedFocusModes.get(0));
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera1.12
                    @Override 
                    public void run() {
                        Camera1.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
            return false;
        }
    }

    private boolean setFlashInternal(int i) {
        try {
            if (isCameraOpened()) {
                List<String> supportedFlashModes = this.mCameraParameters.getSupportedFlashModes();
                SparseArrayCompat<String> sparseArrayCompat = FLASH_MODES;
                String str = sparseArrayCompat.get(i);
                if (supportedFlashModes != null && supportedFlashModes.contains(str)) {
                    this.mCameraParameters.setFlashMode(str);
                    this.mFlash = i;
                    return true;
                }
                String str2 = sparseArrayCompat.get(this.mFlash);
                if (supportedFlashModes != null && supportedFlashModes.contains(str2)) {
                    return false;
                }
                this.mCameraParameters.setFlashMode(DebugKt.DEBUG_PROPERTY_VALUE_OFF);
                this.mFlash = 0;
                return true;
            }
            this.mFlash = i;
            return false;
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera1.13
                    @Override 
                    public void run() {
                        Camera1.this.cameraErrorCallback.onCameraError(e);
                    }
                });
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startBackgroundThread() {
        HandlerThread handlerThread = new HandlerThread("CameraFrameBackground");
        this.mFrameThread = handlerThread;
        handlerThread.start();
        this.mFrameHandler = new Handler(this.mFrameThread.getLooper());
    }

    private void stopBackgroundThread() {
        try {
            HandlerThread handlerThread = this.mFrameThread;
            if (handlerThread != null) {
                handlerThread.quit();
            }
            HandlerThread handlerThread2 = this.mFrameThread;
            if (handlerThread2 != null) {
                handlerThread2.join();
            }
            this.mFrameThread = null;
            this.mFrameHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0055 A[Catch: Exception -> 0x0064, TRY_LEAVE, TryCatch #0 {Exception -> 0x0064, blocks: (B:3:0x0001, B:5:0x0018, B:7:0x001f, B:10:0x0031, B:12:0x0035, B:13:0x0037, B:24:0x0055, B:15:0x003a, B:18:0x004a, B:20:0x004e, B:21:0x0050), top: B:31:0x0001 }] */
    @Override // com.google.android.cameraview.CameraViewImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean zoom(MotionEvent motionEvent) {
        boolean z;
        try {
            Camera.Parameters parameters = this.mCamera.getParameters();
            int maxZoom = parameters.getMaxZoom();
            int zoom = parameters.getZoom();
            float fingerSpacing = getFingerSpacing(motionEvent);
            Float f = this.mZoomDistance;
            if (f == null) {
                this.mZoomDistance = Float.valueOf(fingerSpacing);
                return true;
            }
            int i = (maxZoom / 30) + 1;
            if (fingerSpacing - f.floatValue() >= this.pixelsPerOneZoomLevel) {
                if (zoom < maxZoom) {
                    if (zoom + i > maxZoom) {
                        i = maxZoom - zoom;
                    }
                    zoom += i;
                }
            } else if (this.mZoomDistance.floatValue() - fingerSpacing < this.pixelsPerOneZoomLevel) {
                z = false;
                if (z) {
                    this.mZoomDistance = Float.valueOf(fingerSpacing);
                    parameters.setZoom(zoom);
                    this.mCamera.setParameters(parameters);
                }
                return true;
            } else if (zoom > 0) {
                if (zoom - i < 1) {
                    i = zoom - 1;
                }
                zoom -= i;
            }
            z = true;
            if (z) {
            }
            return true;
        } catch (Exception e) {
            if (this.cameraErrorCallback != null) {
                this.mPreview.getView().post(new Runnable() { // from class: com.google.android.cameraview.Camera1.14
                    @Override 
                    public void run() {
                        Camera1.this.cameraErrorCallback.onCameraError(e);
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
}
