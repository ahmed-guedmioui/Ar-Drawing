package com.google.android.cameraview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.core.os.ParcelableCompat;
import androidx.core.os.ParcelableCompatCreatorCallbacks;
import androidx.core.view.ViewCompat;
import com.google.android.cameraview.CameraViewImpl;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;


public class CameraView extends FrameLayout {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int FACING_BACK = 0;
    public static final int FACING_FRONT = 1;
    public static final int FLASH_AUTO = 3;
    public static final int FLASH_OFF = 0;
    public static final int FLASH_ON = 1;
    public static final int FLASH_RED_EYE = 4;
    public static final int FLASH_TORCH = 2;
    private boolean mAdjustViewBounds;
    private final DisplayOrientationDetector mDisplayOrientationDetector;
    CameraViewImpl mImpl;
    private boolean mZoomEnabled;
    private int maximumPreviewWidth;
    private int maximumWidth;

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface Facing {
    }

    /* loaded from: classes.dex */
    public @interface Flash {
    }

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CameraView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mZoomEnabled = true;
        this.maximumWidth = 0;
        this.maximumPreviewWidth = 0;
        if (isInEditMode()) {
            this.mDisplayOrientationDetector = null;
            return;
        }
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.CameraView, i, R.style.Widget_CameraView);
        this.mAdjustViewBounds = obtainStyledAttributes.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false);
        String string = obtainStyledAttributes.getString(R.styleable.CameraView_cameraAspectRatio);
        this.maximumWidth = obtainStyledAttributes.getInt(R.styleable.CameraView_maximumWidth, 0);
        this.maximumPreviewWidth = obtainStyledAttributes.getInt(R.styleable.CameraView_maximumPreviewWidth, 0);
        boolean z = obtainStyledAttributes.getBoolean(R.styleable.CameraView_useHighResPicture, this.maximumWidth == 0);
        int i2 = obtainStyledAttributes.getInt(R.styleable.CameraView_facing, 0);
        boolean z2 = obtainStyledAttributes.getBoolean(R.styleable.CameraView_autoFocus, true);
        int i3 = obtainStyledAttributes.getInt(R.styleable.CameraView_flash, 3);
        boolean z3 = obtainStyledAttributes.getBoolean(R.styleable.CameraView_enableZoom, true);
        obtainStyledAttributes.recycle();
        PreviewImpl createPreviewImpl = createPreviewImpl(context, false);
        if (CameraViewConfig.isForceCamera1 || Build.VERSION.SDK_INT < 21) {
            this.mImpl = new Camera1(createPreviewImpl, context);
        } else if (Build.VERSION.SDK_INT >= 23 && z) {
            this.mImpl = new Camera2Api23(createPreviewImpl, context);
        } else {
            this.mImpl = new Camera2(createPreviewImpl, context);
        }
        this.mImpl.setMaximumWidth(this.maximumWidth);
        this.mImpl.setMaximumPreviewWidth(this.maximumPreviewWidth);
        setFacing(i2);
        setAutoFocus(z2);
        setFlash(i3);
        setZoomEnabled(z3);
        if (string != null) {
            setAspectRatio(AspectRatio.parse(string), true);
        } else {
            setAspectRatio(Constants.DEFAULT_ASPECT_RATIO, true);
        }
        this.mDisplayOrientationDetector = new DisplayOrientationDetector(context) { // from class: com.google.android.cameraview.CameraView.1
            @Override // com.google.android.cameraview.DisplayOrientationDetector
            public void onDisplayOrientationChanged(int i4) {
                CameraView.this.mImpl.setDisplayOrientation(i4);
            }
        };
    }

    private PreviewImpl createPreviewImpl(Context context, boolean z) {
        if (Build.VERSION.SDK_INT < 21 || CameraViewConfig.isForceCamera1 || z) {
            return new SurfaceViewPreview(context, this);
        }
        return new TextureViewPreview(context, this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            return;
        }
        this.mDisplayOrientationDetector.enable(ViewCompat.getDisplay(this));
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            this.mDisplayOrientationDetector.disable();
        }
        super.onDetachedFromWindow();
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        if (isInEditMode()) {
            super.onMeasure(i, i2);
            return;
        }
        if (this.mAdjustViewBounds) {
            if (!isCameraOpened()) {
                super.onMeasure(i, i2);
                return;
            }
            int mode = View.MeasureSpec.getMode(i);
            int mode2 = View.MeasureSpec.getMode(i2);
            if (mode == 1073741824 && mode2 != 1073741824) {
                int size = (int) (View.MeasureSpec.getSize(i) * getAspectRatio().toFloat());
                if (mode2 == Integer.MIN_VALUE) {
                    size = Math.min(size, View.MeasureSpec.getSize(i2));
                }
                super.onMeasure(i, View.MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
            } else if (mode != 1073741824 && mode2 == 1073741824) {
                int size2 = (int) (View.MeasureSpec.getSize(i2) * getAspectRatio().toFloat());
                if (mode == Integer.MIN_VALUE) {
                    size2 = Math.min(size2, View.MeasureSpec.getSize(i));
                }
                super.onMeasure(View.MeasureSpec.makeMeasureSpec(size2, MeasureSpec.EXACTLY), i2);
            } else {
                super.onMeasure(i, i2);
            }
        } else {
            super.onMeasure(i, i2);
        }
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        AspectRatio aspectRatio = getAspectRatio();
        if (this.mDisplayOrientationDetector.getLastKnownDisplayOrientation() % 180 == 0) {
            aspectRatio = aspectRatio.inverse();
        }
        if (measuredHeight < (aspectRatio.getY() * measuredWidth) / aspectRatio.getX()) {
            this.mImpl.getView().measure(View.MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec((measuredWidth * aspectRatio.getY()) / aspectRatio.getX(), MeasureSpec.EXACTLY));
        } else {
            this.mImpl.getView().measure(View.MeasureSpec.makeMeasureSpec((aspectRatio.getX() * measuredHeight) / aspectRatio.getY(), MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mImpl == null) {
            return super.onTouchEvent(motionEvent);
        }
        int actionMasked = motionEvent.getActionMasked();
        if (motionEvent.getPointerCount() == 2 && this.mZoomEnabled) {
            if (actionMasked == 6 || actionMasked == 1) {
                this.mImpl.onPinchFingerUp();
            } else {
                this.mImpl.zoom(motionEvent);
            }
        }
        return true;
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.facing = getFacing();
        savedState.ratio = getAspectRatio();
        savedState.autoFocus = getAutoFocus();
        savedState.flash = getFlash();
        savedState.zoom = isZoomEnabled();
        return savedState;
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setFacing(savedState.facing);
        setAspectRatio(savedState.ratio, true);
        setAutoFocus(savedState.autoFocus);
        setFlash(savedState.flash);
        setZoomEnabled(savedState.zoom);
    }

    public void start() {
        if (this.mImpl.start()) {
            return;
        }
        Parcelable onSaveInstanceState = onSaveInstanceState();
        Camera1 camera1 = new Camera1(createPreviewImpl(getContext(), true), getContext());
        this.mImpl = camera1;
        camera1.setMaximumWidth(this.maximumWidth);
        this.mImpl.setMaximumPreviewWidth(this.maximumPreviewWidth);
        onRestoreInstanceState(onSaveInstanceState);
        this.mImpl.start();
    }

    public void stop() {
        this.mImpl.stop();
    }

    public boolean isCameraOpened() {
        return this.mImpl.isCameraOpened();
    }

    public void setAdjustViewBounds(boolean z) {
        if (this.mAdjustViewBounds != z) {
            this.mAdjustViewBounds = z;
            requestLayout();
        }
    }

    public boolean getAdjustViewBounds() {
        return this.mAdjustViewBounds;
    }

    public void setFacing(int i) {
        this.mImpl.setFacing(i);
    }

    public void switchCamera() {
        if (getFacing() == 0) {
            setFacing(1);
        } else {
            setFacing(0);
        }
    }

    public int getFacing() {
        return this.mImpl.getFacing();
    }

    public Set<AspectRatio> getSupportedAspectRatios() {
        return this.mImpl.getSupportedAspectRatios();
    }

    public void setAspectRatio(AspectRatio aspectRatio, boolean z) {
        if (this.mImpl.setAspectRatio(aspectRatio, z)) {
            requestLayout();
        }
    }

    public AspectRatio getAspectRatio() {
        return this.mImpl.getAspectRatio();
    }

    public void setAutoFocus(boolean z) {
        this.mImpl.setAutoFocus(z);
    }

    public boolean getAutoFocus() {
        return this.mImpl.getAutoFocus();
    }

    public void setFlash(int i) {
        this.mImpl.setFlash(i);
    }

    public int getFlash() {
        return this.mImpl.getFlash();
    }

    public Size getPreviewSize() {
        return this.mImpl.getPreviewSize();
    }

    public Size getPictureSize() {
        return this.mImpl.getPictureSize();
    }

    public void setOnPictureTakenListener(CameraViewImpl.OnPictureTakenListener onPictureTakenListener) {
        CameraViewImpl cameraViewImpl = this.mImpl;
        if (cameraViewImpl != null) {
            cameraViewImpl.setOnPictureTakenListener(onPictureTakenListener);
        }
    }

    public void setOnPictureBytesAvailableListener(CameraViewImpl.OnPictureBytesAvailableListener onPictureBytesAvailableListener) {
        CameraViewImpl cameraViewImpl = this.mImpl;
        if (cameraViewImpl != null) {
            cameraViewImpl.setOnPictureBytesAvailableListener(onPictureBytesAvailableListener);
        }
    }

    public void setOnFocusLockedListener(CameraViewImpl.OnFocusLockedListener onFocusLockedListener) {
        CameraViewImpl cameraViewImpl = this.mImpl;
        if (cameraViewImpl != null) {
            cameraViewImpl.setOnFocusLockedListener(onFocusLockedListener);
        }
    }

    public void setOnTurnCameraFailListener(CameraViewImpl.OnTurnCameraFailListener onTurnCameraFailListener) {
        CameraViewImpl cameraViewImpl = this.mImpl;
        if (cameraViewImpl != null) {
            cameraViewImpl.setOnTurnCameraFailListener(onTurnCameraFailListener);
        }
    }

    public void setOnCameraErrorListener(CameraViewImpl.OnCameraErrorListener onCameraErrorListener) {
        CameraViewImpl cameraViewImpl = this.mImpl;
        if (cameraViewImpl != null) {
            cameraViewImpl.setOnCameraErrorListener(onCameraErrorListener);
        }
    }

    public void setOnFrameListener(CameraViewImpl.OnFrameListener onFrameListener) {
        CameraViewImpl cameraViewImpl = this.mImpl;
        if (cameraViewImpl != null) {
            cameraViewImpl.setOnFrameListener(onFrameListener);
        }
    }

    public void takePicture() {
        this.mImpl.takePicture();
    }

    public void setPixelsPerOneZoomLevel(int i) {
        CameraViewImpl cameraViewImpl = this.mImpl;
        if (cameraViewImpl != null) {
            cameraViewImpl.setPixelsPerOneZoomLevel(i);
        }
    }

    public void setZoomEnabled(boolean z) {
        this.mZoomEnabled = z;
    }

    public boolean isZoomEnabled() {
        return this.mZoomEnabled;
    }

    public int getDefaultOrientation() {
        CameraViewImpl cameraViewImpl = this.mImpl;
        if (cameraViewImpl != null) {
            return cameraViewImpl.getCameraDefaultOrientation();
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() { // from class: com.google.android.cameraview.CameraView.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // androidx.core.os.ParcelableCompatCreatorCallbacks
            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // androidx.core.os.ParcelableCompatCreatorCallbacks
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        });
        boolean autoFocus;
        int facing;
        int flash;
        AspectRatio ratio;
        boolean zoom;

        public SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel);
            this.facing = parcel.readInt();
            this.ratio = (AspectRatio) parcel.readParcelable(classLoader);
            this.autoFocus = parcel.readByte() != 0;
            this.flash = parcel.readInt();
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.facing);
            parcel.writeParcelable(this.ratio, 0);
            parcel.writeByte(this.autoFocus ? (byte) 1 : (byte) 0);
            parcel.writeInt(this.flash);
        }
    }
}
