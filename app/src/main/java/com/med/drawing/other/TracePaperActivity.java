package com.med.drawing.other;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.view.ViewCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.med.drawing.R;
import com.thebluealliance.spectrum.SpectrumDialog;


public class TracePaperActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE_CAMERA = 3002;
    RelativeLayout CameraPic;
    RelativeLayout GalleryPic;
    RelativeLayout ImageEdit;
    RelativeLayout ImageLock;
    LottieAnimationView animation_view;
    Bitmap bmOriginal;
    int brightness;
    SeekBar brightness_seekbar;
    private ContentResolver cResolver;
    ImageView img_bg;
    ImageView img_lock;
    RelativeLayout main_layout;
    ImageView objectImage;
    SeekBar opacity_seekbar;
    Animation pushanim;
    RelativeLayout rel_flip;
    private Window window;
    boolean is_lock = false;
    boolean is_edit_sketch = false;
    boolean isblack = true;

    @Override

    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.activity_trace_paper);
        this.pushanim = AnimationUtils.loadAnimation(this, R.anim.view_push);
        this.main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        this.rel_flip = (RelativeLayout) findViewById(R.id.rel_flip);
        this.CameraPic = (RelativeLayout) findViewById(R.id.rel_camera);
        this.GalleryPic = (RelativeLayout) findViewById(R.id.rel_gallery);
        this.ImageLock = (RelativeLayout) findViewById(R.id.rel_lock);
        this.ImageEdit = (RelativeLayout) findViewById(R.id.rel_edit_round);
        this.opacity_seekbar = (SeekBar) findViewById(R.id.alpha_seek);
        this.brightness_seekbar = (SeekBar) findViewById(R.id.brightness_seek);
        this.img_lock = (ImageView) findViewById(R.id.ic_lock);
        this.objectImage = (ImageView) findViewById(R.id.objImage);
        this.img_bg = (ImageView) findViewById(R.id.img_bg);
        this.animation_view = (LottieAnimationView) findViewById(R.id.animation_view);
        int i = Resources.getSystem().getDisplayMetrics().widthPixels;
        String string = getIntent().getExtras().getString("ImagePath");
        this.isblack = true;
        this.main_layout.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        Window window = getWindow();
        this.window = window;
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.screenBrightness = 1.0f;
        getWindow().setAttributes(attributes);
        this.cResolver = getContentResolver();
        this.brightness_seekbar.setMax(255);
        this.brightness_seekbar.setProgress(255);
        this.brightness_seekbar.setKeyProgressIncrement(1);
        try {
            this.brightness = Settings.System.getInt(this.cResolver, "screen_brightness");
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }
        this.brightness_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override 
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override 
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override 
            public void onProgressChanged(SeekBar seekBar, int i2, boolean z) {
                if (i2 <= 20) {
                    TracePaperActivity.this.brightness = 20;
                } else {
                    TracePaperActivity.this.brightness = i2;
                }
                WindowManager.LayoutParams attributes2 = TracePaperActivity.this.window.getAttributes();
                attributes2.screenBrightness = TracePaperActivity.this.brightness / 255.0f;
                TracePaperActivity.this.window.setAttributes(attributes2);
            }
        });
        if (string != null) {
            if (string.contains("sketch_drawing")) {
                this.bmOriginal = AppConstant.getBitmapFromAsset(this, string);
            } else {
                this.bmOriginal = AppConstant.getBitmap(string);
            }
            ImageView imageView = this.objectImage;
            double d = (double) i;
            Double.isNaN(d);

            imageView.setOnTouchListener(new MultiTouch(imageView, 1.0f, 1.0f, (float) ((int) (d / 3.5d)), 600.0f));

            Bitmap bitmap = this.bmOriginal;
            if (bitmap != null) {
                this.objectImage.setImageBitmap(bitmap);
                this.is_edit_sketch = false;
            } else {
                Toast.makeText(this, "Some issue with this image try another one.", Toast.LENGTH_SHORT).show();
            }
        }
        this.animation_view.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override 
            public void run() {
                TracePaperActivity.this.animation_view.setVisibility(View.GONE);
            }
        }, 7000L);
        this.CameraPic.setOnClickListener(new View.OnClickListener() {
            @Override 
            public void onClick(View view) {
                view.startAnimation(TracePaperActivity.this.pushanim);
                ImagePicker.with(TracePaperActivity.this).cameraOnly().saveDir(TracePaperActivity.this.getExternalFilesDir(Environment.DIRECTORY_DCIM)).start(103);


            }
        });
        this.GalleryPic.setOnClickListener(new View.OnClickListener() {
            @Override 
            public void onClick(View view) {
                view.startAnimation(TracePaperActivity.this.pushanim);
                ImagePicker.with(TracePaperActivity.this).galleryOnly().start(102);


            }
        });
        this.rel_flip.setOnClickListener(new View.OnClickListener() {
            @Override 
            public void onClick(View view) {
                view.startAnimation(TracePaperActivity.this.pushanim);
                TracePaperActivity tracePaperActivity = TracePaperActivity.this;
                tracePaperActivity.bmOriginal = TracePaperActivity.flip(tracePaperActivity.bmOriginal, 2);
                if (TracePaperActivity.this.bmOriginal != null) {
                    TracePaperActivity.this.objectImage.setImageBitmap(TracePaperActivity.this.bmOriginal);
                }
            }
        });
        this.ImageEdit.setOnClickListener(new View.OnClickListener() {
            @Override 
            public void onClick(View view) {
                view.startAnimation(TracePaperActivity.this.pushanim);
                TracePaperActivity.this.ColorDialog();
            }
        });
        this.ImageLock.setOnClickListener(new View.OnClickListener() {
            @Override 
            public void onClick(View view) {
                view.startAnimation(TracePaperActivity.this.pushanim);
                if (!TracePaperActivity.this.is_lock) {
                    TracePaperActivity.this.objectImage.setEnabled(false);
                    TracePaperActivity.this.is_lock = true;
                    TracePaperActivity.this.img_lock.setImageResource(R.drawable.unlock);
                    return;
                }
                TracePaperActivity.this.objectImage.setEnabled(true);
                TracePaperActivity.this.is_lock = false;
                TracePaperActivity.this.img_lock.setImageResource(R.drawable.lock);
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
                TracePaperActivity.this.objectImage.setAlpha((TracePaperActivity.this.opacity_seekbar.getMax() - i2) / 10.0f);
            }
        });
    }

    public void ColorDialog() {
        new SpectrumDialog.Builder(this).setColors(R.array.demo_colors).setSelectedColorRes(R.color.black).setDismissOnColorSelected(true).setOutlineWidth(2).setFixedColumnCount(4).setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
            @Override
            public void onColorSelected(boolean z, int i) {
                if (z) {
                    TracePaperActivity.this.main_layout.setBackgroundColor(i);
                }
            }
        }).build().show(getSupportFragmentManager(), "color");
    }

    private void openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= 23) {
            Intent intent = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    public static Bitmap flip(Bitmap bitmap, int i) {
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
    @Override
    public void onResume() {
        super.onResume();
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
    }
}
