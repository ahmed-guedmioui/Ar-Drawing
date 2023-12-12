package com.google.android.cameraview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.cameraview.Orientation;
import java.util.Set;


public abstract class CameraViewImpl {
    protected OnCameraErrorListener cameraErrorCallback;
    protected int currentOrientationDegrees;
    protected OnFocusLockedListener focusLockedCallback;
    protected Size mPictureSizeSelected;
    protected final PreviewImpl mPreview;
    protected Size mPreviewSizeSelected;
    protected OnFrameListener onFrameCallback;
    protected Orientation orientation;
    protected OnPictureBytesAvailableListener pictureBytesCallback;
    protected OnPictureTakenListener pictureCallback;
    protected OnTurnCameraFailListener turnFailCallback;
    protected int pixelsPerOneZoomLevel = 80;
    protected int maximumWidth = 0;
    protected int maximumPreviewWidth = 0;
    protected Orientation.Listener orientationListener = new Orientation.Listener() { // from class: com.google.android.cameraview.CameraViewImpl.1
        @Override // com.google.android.cameraview.Orientation.Listener
        public void onOrientationChanged(float f, float f2) {
            CameraViewImpl cameraViewImpl = CameraViewImpl.this;
            cameraViewImpl.currentOrientationDegrees = cameraViewImpl.pitchAndRollToDegrees(f, f2);
        }
    };

    
    public interface OnCameraErrorListener {
        void onCameraError(Exception exc);
    }

    
    public interface OnFocusLockedListener {
        void onFocusLocked();
    }

    
    public interface OnFrameListener {
        void onFrame(byte[] bArr, int i, int i2, int i3);
    }

    
    public interface OnPictureBytesAvailableListener {
        void onPictureBytesAvailable(byte[] bArr, int i);
    }

    
    public interface OnPictureTakenListener {
        void onPictureTaken(Bitmap bitmap, int i);
    }

    
    public interface OnTurnCameraFailListener {
        void onTurnCameraFail(Exception exc);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int pitchAndRollToDegrees(float f, float f2) {
        if (f2 < -135.0f || f2 > 135.0f) {
            return 180;
        }
        return (f2 <= 45.0f || f2 > 135.0f) ? (f2 < -135.0f || f2 >= -45.0f) ? 0 : 90 : Constants.LANDSCAPE_270;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract AspectRatio getAspectRatio();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean getAutoFocus();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract int getCameraDefaultOrientation();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract int getFacing();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract int getFlash();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract Set<AspectRatio> getSupportedAspectRatios();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean isCameraOpened();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void onPinchFingerUp();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean setAspectRatio(AspectRatio aspectRatio, boolean z);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setAutoFocus(boolean z);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setDisplayOrientation(int i);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setFacing(int i);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setFlash(int i);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean start();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void stop();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void takePicture();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean zoom(MotionEvent motionEvent);

    /* JADX INFO: Access modifiers changed from: package-private */
    public CameraViewImpl(PreviewImpl previewImpl, Context context) {
        this.mPreview = previewImpl;
        this.orientation = new Orientation(context, 100);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public View getView() {
        return this.mPreview.getView();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bitmap mirrorBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void setOnPictureTakenListener(OnPictureTakenListener onPictureTakenListener) {
        this.pictureCallback = onPictureTakenListener;
    }

    public void setOnPictureBytesAvailableListener(OnPictureBytesAvailableListener onPictureBytesAvailableListener) {
        this.pictureBytesCallback = onPictureBytesAvailableListener;
    }

    public void setOnFocusLockedListener(OnFocusLockedListener onFocusLockedListener) {
        this.focusLockedCallback = onFocusLockedListener;
    }

    public void setOnTurnCameraFailListener(OnTurnCameraFailListener onTurnCameraFailListener) {
        this.turnFailCallback = onTurnCameraFailListener;
    }

    public void setOnCameraErrorListener(OnCameraErrorListener onCameraErrorListener) {
        this.cameraErrorCallback = onCameraErrorListener;
    }

    public void setOnFrameListener(OnFrameListener onFrameListener) {
        this.onFrameCallback = onFrameListener;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public float getFingerSpacing(MotionEvent motionEvent) {
        float x = motionEvent.getX(0) - motionEvent.getX(1);
        float y = motionEvent.getY(0) - motionEvent.getY(1);
        return (float) Math.sqrt((x * x) + (y * y));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void byteArrayToBitmap(final byte[] bArr) {
        if (this.pictureCallback == null) {
            return;
        }
        AsyncTask.execute(new Runnable() { // from class: com.google.android.cameraview.CameraViewImpl.2
            @Override 
            public void run() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                byte[] bArr2 = bArr;
                Bitmap decodeByteArray = BitmapFactory.decodeByteArray(bArr2, 0, bArr2.length, options);
                if (CameraViewImpl.this.getFacing() == 1) {
                    if (CameraViewImpl.this.pictureCallback != null) {
                        CameraViewImpl.this.pictureCallback.onPictureTaken(CameraViewImpl.this.mirrorBitmap(decodeByteArray), CameraViewImpl.this.getRotationDegrees());
                    }
                } else if (CameraViewImpl.this.pictureCallback != null) {
                    CameraViewImpl.this.pictureCallback.onPictureTaken(decodeByteArray, CameraViewImpl.this.getRotationDegrees());
                }
            }
        });
    }

    public void setPixelsPerOneZoomLevel(int i) {
        if (i <= 0) {
            return;
        }
        this.pixelsPerOneZoomLevel = i;
    }

    public int getMaximumWidth() {
        return this.maximumWidth;
    }

    public void setMaximumWidth(int i) {
        this.maximumWidth = i;
    }

    public int getMaximumPreviewWidth() {
        return this.maximumPreviewWidth;
    }

    public void setMaximumPreviewWidth(int i) {
        this.maximumPreviewWidth = i;
    }

    public Size getPreviewSize() {
        return this.mPreviewSizeSelected;
    }

    public Size getPictureSize() {
        return this.mPictureSizeSelected;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public int getRotationDegrees() {
        return -(this.currentOrientationDegrees + getCameraDefaultOrientation());
    }
}
