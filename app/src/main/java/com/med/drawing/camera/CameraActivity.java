package com.med.drawing.camera;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.cameraview.CameraView;
import com.google.android.cameraview.CameraViewImpl;
import com.med.drawing.R;
import com.med.drawing.other.AppConstant;
import com.med.drawing.other.FileUtils;
import com.med.drawing.other.MultiTouch;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.Callable;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageThresholdEdgeDetectionFilter;


public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_IMAGE_REQ_CODE = 103;
    static final int FLIP_HORIZONTAL = 2;
    static final int FLIP_VERTICAL = 1;
    private static final int GALLERY_IMAGE_REQ_CODE = 102;
    private static final int PERMISSION_CODE_CAMERA = 3002;
    RelativeLayout CameraFlash;
    RelativeLayout CameraPic;
    RelativeLayout GalleryPic;
    RelativeLayout ImageEdit;
    RelativeLayout ImageLock;
    LottieAnimationView animation_view;
    Bitmap bmOriginal;
    CameraView cameraView;
    ImageView img_flash;
    ImageView img_lock;
    ImageView img_outline;
    private boolean isFlashSupported;
    ImageView objectImage;
    SeekBar opacity_seekbar;
    Animation pushanim;
    RelativeLayout rel_flip;
    ProgressDialog ringProgressDialog;
    private boolean isTorchOn = false;
    boolean is_lock = false;
    boolean is_edit_sketch = false;
    private boolean frameIsProcessing = false;
    Bitmap ConvertedBitmap = null;

    @Override 
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.activity_camera);
        this.pushanim = AnimationUtils.loadAnimation(this, R.anim.view_push);
        this.cameraView = (CameraView) findViewById(R.id.camera_view);
        this.CameraPic = (RelativeLayout) findViewById(R.id.rel_camera);
        this.GalleryPic = (RelativeLayout) findViewById(R.id.rel_gallery);
        this.rel_flip = (RelativeLayout) findViewById(R.id.rel_flip);
        this.CameraFlash = (RelativeLayout) findViewById(R.id.rel_flash);
        this.ImageLock = (RelativeLayout) findViewById(R.id.rel_lock);
        this.ImageEdit = (RelativeLayout) findViewById(R.id.rel_edit_round);
        this.opacity_seekbar = (SeekBar) findViewById(R.id.alpha_seek);
        this.img_lock = (ImageView) findViewById(R.id.ic_lock);
        this.img_flash = (ImageView) findViewById(R.id.ic_flash);
        this.objectImage = (ImageView) findViewById(R.id.objImage);
        this.img_outline = (ImageView) findViewById(R.id.img_outline);
        this.animation_view = (LottieAnimationView) findViewById(R.id.animation_view);
        int i = Resources.getSystem().getDisplayMetrics().widthPixels;
        setupFlashButton();
        String string = getIntent().getExtras().getString("ImagePath");
        if (string != null) {
            if (string.contains("sketch_drawing")) {
                this.bmOriginal = AppConstant.getBitmapFromAsset(this, string);
            } else {
                this.bmOriginal = AppConstant.getBitmap(string);
            }
            ImageView imageView = this.objectImage;
            double d = (double) i;

            Double.isNaN(i);
            imageView.setOnTouchListener(new MultiTouch(imageView, 1.0f, 1.0f, (float) ((int) (d / 3.5d)), 600.0f));

            Bitmap bitmap = this.bmOriginal;
            if (bitmap != null) {
                this.objectImage.setImageBitmap(bitmap);
                this.is_edit_sketch = false;
                this.img_outline.setImageResource(R.drawable.outline);
            } else {
                Toast.makeText(this, "Some issue with this image try another one.", Toast.LENGTH_SHORT).show();
            }
            this.objectImage.setAlpha(0.6f);
            this.opacity_seekbar.setProgress(4);
        }
        this.animation_view.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { 
            @Override 
            public void run() {
                CameraActivity.this.animation_view.setVisibility(View.GONE);
            }
        }, 7000L);
        this.CameraPic.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                view.startAnimation(CameraActivity.this.pushanim);
                ImagePicker.with(CameraActivity.this).cameraOnly().saveDir(CameraActivity.this.getExternalFilesDir(Environment.DIRECTORY_DCIM)).start(103);
              }
        });
        this.GalleryPic.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                view.startAnimation(CameraActivity.this.pushanim);
                ImagePicker.with(CameraActivity.this).galleryOnly().start(102);

            }
        });
        this.rel_flip.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                view.startAnimation(CameraActivity.this.pushanim);
                CameraActivity cameraActivity = CameraActivity.this;
                cameraActivity.bmOriginal = CameraActivity.flip(cameraActivity.bmOriginal, 2);
                if (CameraActivity.this.bmOriginal != null) {
                    CameraActivity.this.objectImage.setImageBitmap(CameraActivity.this.bmOriginal);
                }
            }
        });
        this.ImageEdit.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                CameraActivity.this.ConvertBorderBitmap();
            }
        });
        this.ImageLock.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                if (!CameraActivity.this.is_lock) {
                    CameraActivity.this.objectImage.setEnabled(false);
                    CameraActivity.this.is_lock = true;
                    CameraActivity.this.img_lock.setImageResource(R.drawable.unlock);
                    return;
                }
                CameraActivity.this.objectImage.setEnabled(true);
                CameraActivity.this.is_lock = false;
                CameraActivity.this.img_lock.setImageResource(R.drawable.lock);
            }
        });
        this.opacity_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override 
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override 
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override 
            public void onProgressChanged(SeekBar seekBar, int i2, boolean z) {
                CameraActivity.this.objectImage.setAlpha((CameraActivity.this.opacity_seekbar.getMax() - i2) / 10.0f);
            }
        });
        this.CameraFlash.setOnClickListener(new View.OnClickListener() {
            @Override 
            public void onClick(View view) {
                CameraActivity.this.switchFlash();
            }
        });
    }

    public void ConvertBorderBitmap() {
        final GPUImage gPUImage = new GPUImage(this);
        ProgressDialog show = ProgressDialog.show(this, "", "Convert Bitmap", true);
        this.ringProgressDialog = show;
        show.setCancelable(false);
        new Thread(new Runnable() {
            @Override 
            public void run() {
                try {
                    if (!CameraActivity.this.is_edit_sketch) {
                        gPUImage.setImage(CameraActivity.this.bmOriginal);
                        gPUImage.setFilter(new GPUImageThresholdEdgeDetectionFilter());
                        Bitmap bitmapWithFilterApplied = gPUImage.getBitmapWithFilterApplied();
                        if (bitmapWithFilterApplied != null) {
                            CameraActivity.this.ConvertedBitmap = CameraActivity.getBitmapWithTransparentBG(bitmapWithFilterApplied, -1);
                        } else {
                            Toast.makeText(CameraActivity.this, "Can't Convert this image try with another", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception unused) {
                }
                CameraActivity.this.ringProgressDialog.dismiss();
            }
        }).start();
        this.ringProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (!CameraActivity.this.is_edit_sketch) {
                    if (CameraActivity.this.ConvertedBitmap != null) {
                        CameraActivity.this.is_edit_sketch = true;
                        CameraActivity.this.objectImage.setImageBitmap(CameraActivity.this.ConvertedBitmap);
                        CameraActivity.this.img_outline.setImageResource(R.drawable.normal);
                        return;
                    }
                    Toast.makeText(CameraActivity.this, "Can't Convert this image try with another", Toast.LENGTH_SHORT).show();
                } else if (CameraActivity.this.bmOriginal != null) {
                    CameraActivity.this.is_edit_sketch = false;
                    CameraActivity.this.objectImage.setImageBitmap(CameraActivity.this.bmOriginal);
                    CameraActivity.this.img_outline.setImageResource(R.drawable.outline);
                } else {
                    Toast.makeText(CameraActivity.this, "Can't Convert this image try with another", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static Bitmap flip(Bitmap bitmap, int i) {
        if (bitmap != null) {
            Matrix matrix = new Matrix();
            if (i == 1) {
                matrix.preScale(1.0f, -1.0f);
            } else if (i != 2) {
                return null;
            } else {
                matrix.preScale(-1.0f, 1.0f);
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    public static Bitmap getBitmapWithTransparentBG(Bitmap bitmap, int i) {
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int width = copy.getWidth();
        int height = copy.getHeight();
        for (int i2 = 0; i2 < height; i2++) {
            for (int i3 = 0; i3 < width; i3++) {
                if (copy.getPixel(i3, i2) == i) {
                    copy.setPixel(i3, i2, 0);
                }
            }
        }
        return copy;
    }

    public void setupFlashButton() {
        try {
            this.isFlashSupported = ((Boolean) ((CameraManager) getSystemService(Context.CAMERA_SERVICE)).getCameraCharacteristics("0").get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (this.isFlashSupported) {
            this.CameraFlash.setVisibility(View.VISIBLE);
            if (!this.isTorchOn) {
                this.img_flash.setImageResource(R.drawable.ic_flash_off);
                return;
            } else {
                this.img_flash.setImageResource(R.drawable.ic_flash_on);
                return;
            }
        }
        this.CameraFlash.setVisibility(View.GONE);
    }

    public void switchFlash() {
        try {
            this.isFlashSupported = ((Boolean) ((CameraManager) getSystemService(Context.CAMERA_SERVICE)).getCameraCharacteristics("0").get(CameraCharacteristics.FLASH_INFO_AVAILABLE)).booleanValue();
            if (this.isTorchOn) {
                this.isTorchOn = false;
                this.img_flash.setImageResource(R.drawable.ic_flash_off);
                this.cameraView.setFlash(CameraView.FLASH_OFF);
            } else {
                this.isTorchOn = true;
                this.img_flash.setImageResource(R.drawable.ic_flash_on);
                this.cameraView.setFlash(CameraView.FLASH_TORCH);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        String realPathFromURI_API19;
        super.onActivityResult(i, i2, intent);
        if (i2 == -1) {
            Uri data = intent.getData();
            if (i != 102) {
                if (i != 103) {
                    return;
                }
                Bitmap bitmap = AppConstant.getBitmap(FileUtils.getPath(data));
                this.bmOriginal = bitmap;
                this.objectImage.setImageBitmap(bitmap);
                this.is_edit_sketch = false;
                return;
            }
            if (Build.VERSION.SDK_INT < 11) {
                realPathFromURI_API19 = AppConstant.getRealPathFromURI_BelowAPI11(this, data);
            } else if (Build.VERSION.SDK_INT < 19) {
                realPathFromURI_API19 = AppConstant.getRealPathFromURI_API11to18(this, data);
            } else {
                realPathFromURI_API19 = AppConstant.getRealPathFromURI_API19(this, data);
            }
            Bitmap bitmap2 = AppConstant.getBitmap(realPathFromURI_API19);
            this.bmOriginal = bitmap2;
            this.objectImage.setImageBitmap(bitmap2);
            this.is_edit_sketch = false;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bitmap, int i, int i2) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(i / width, i2 / height);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        return createBitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PermissionUtils.isCameraGranted(this)) {
            this.cameraView.start();
            this.cameraView.clearFocus();
            setupCameraCallbacks();
        } else if (PermissionUtils.isCameraGranted(this)) {
        } else {
            PermissionUtils.checkPermission(this, "android.permission.CAMERA", PERMISSION_CODE_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == PERMISSION_CODE_CAMERA && (iArr.length <= 0 || iArr[0] != 0)) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (i != PERMISSION_CODE_CAMERA) {
            super.onRequestPermissionsResult(i, strArr, iArr);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraView cameraView = this.cameraView;
        if (cameraView != null) {
            cameraView.stop();
        }
        ProgressDialog progressDialog = this.ringProgressDialog;
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        this.ringProgressDialog.dismiss();
    }

    private void setupCameraCallbacks() {
        this.cameraView.setOnPictureTakenListener(new CameraViewImpl.OnPictureTakenListener() { 
            @Override 
            public void onPictureTaken(Bitmap bitmap, int i) {
            }
        });
        this.cameraView.setOnFocusLockedListener(new CameraViewImpl.OnFocusLockedListener() { 
            @Override 
            public void onFocusLocked() {
            }
        });
        this.cameraView.setOnTurnCameraFailListener(new CameraViewImpl.OnTurnCameraFailListener() { 
            @Override 
            public void onTurnCameraFail(Exception exc) {
                Toast.makeText(CameraActivity.this, "Switch Camera Failed. Does you device has a front camera?", Toast.LENGTH_SHORT).show();
            }
        });
        this.cameraView.setOnCameraErrorListener(new CameraViewImpl.OnCameraErrorListener() { 
            @Override 
            public void onCameraError(Exception exc) {
                Toast.makeText(CameraActivity.this, exc.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        this.cameraView.setOnFrameListener(new CameraViewImpl.OnFrameListener() { 
            @Override 
            public void onFrame(final byte[] bArr, int i, int i2, int i3) {
                if (CameraActivity.this.frameIsProcessing) {
                    return;
                }
                CameraActivity.this.frameIsProcessing = true;
                Observable.fromCallable(new Callable<Bitmap>() { 
                    @Override 
                    public Bitmap call() throws Exception {
                        byte[] bArr2 = bArr;
                        return BitmapFactory.decodeByteArray(bArr2, 0, bArr2.length);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Bitmap>() { // from class: com.med.drawing.camera.CameraActivity.15.1
                    @Override 
                    public void onError(Throwable th) {
                    }

                    @Override 
                    public void onSubscribe(Disposable disposable) {
                    }

                    @Override 
                    public void onNext(Bitmap bitmap) {
                        if (bitmap != null) {
                            Log.i("onFrame", bitmap.getWidth() + ", " + bitmap.getHeight());
                        }
                    }

                    @Override 
                    public void onComplete() {
                        CameraActivity.this.frameIsProcessing = false;
                    }
                });
            }
        });
    }
}
