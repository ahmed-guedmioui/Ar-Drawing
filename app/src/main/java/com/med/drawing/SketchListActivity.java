package com.med.drawing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.dhaval2404.imagepicker.ImagePicker;

import com.med.drawing.Adapter.DrawingListAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SketchListActivity extends AppCompatActivity {

    String SelectedImagePath;
    public int STORAGE_PERMISSION_REQUEST_CODE = 12;

    DrawingListAdapter drawing_adapter;
    RelativeLayout help_layout;

    Animation push_animation;


    RecyclerView rv_drawing_list;
    String action_name = "back";
    String BACK = "back";
    int selected_image_position = 0;
    String selected_image_name = "";
    ArrayList<String> drawing_list = new ArrayList<>();


    public boolean isWriteStoragePermissionGranted() {

        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED && checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED && checkSelfPermission("android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_MEDIA_IMAGES", "android.permission.CAMERA"}, this.STORAGE_PERMISSION_REQUEST_CODE);
        return false;
    }

    public void showDailog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need to give permission to access feature.");
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("GIVE PERMISSION", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                ActivityCompat.requestPermissions(SketchListActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAG", "android.permission.READ_MEDIA_IMAGES"}, STORAGE_PERMISSION_REQUEST_CODE);

            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != STORAGE_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == 0 && grantResults[1] == 0) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
            return;
        }
        ImagePicker.with(SketchListActivity.this).galleryOnly().start();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AppConstants.overridePendingTransitionEnter(this);
        setContentView(R.layout.activity_sketch_list);
        AdsManager.getInstance().loadBanner(SketchListActivity.this, getString(R.string.AdMob_Banner));

        this.push_animation = AnimationUtils.loadAnimation(this, R.anim.view_push);

        this.drawing_list = addAssetsImages("sketch_drawing");
        this.help_layout = (RelativeLayout) findViewById(R.id.rel_help);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_drawing_list);
        this.rv_drawing_list = recyclerView;
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        this.rv_drawing_list.setHasFixedSize(true);
        this.rv_drawing_list.setLayoutManager(gridLayoutManager);
        this.help_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(SketchListActivity.this.push_animation);
                if (AppConstant.selected_id.equals(AppConstant.TraceDirect)) {
                    SketchListActivity.this.HelpScreen();
                } else {
                    SketchListActivity.this.HelpScreen2();
                }
            }
        });
        DrawingListAdapter drawingListAdapter = new DrawingListAdapter(this, this.drawing_list) {
            @Override
            public void onDrawingListClickItem(int i, View view) {
                SketchListActivity sketchListActivity = SketchListActivity.this;
                sketchListActivity.SelectedImagePath = sketchListActivity.drawing_list.get(i);
                if (AppConstant.selected_id.equals(AppConstant.TraceDirect)) {
                    SketchListActivity.this.action_name = AppConstant.TraceDirect;
                } else {
                    SketchListActivity.this.action_name = AppConstant.TracePaper;
                }
                SketchListActivity sketchListActivity2 = SketchListActivity.this;
                sketchListActivity2.SelectedImagePath = sketchListActivity2.drawing_list.get(i);
                SketchListActivity.this.selected_image_position = i;
                SketchListActivity sketchListActivity3 = SketchListActivity.this;
                sketchListActivity3.selected_image_name = "Image_" + i;
                if (SketchListActivity.this.selected_image_position > 5) {
                    if (AppConstant.selected_id.equals(AppConstant.TraceDirect)) {
                        SketchListActivity.this.TraceDrawingScreen();
                    } else {
                        SketchListActivity.this.TracePaperScreen();
                    }
                } else {
                    SketchListActivity.this.TraceDrawingScreen();
                }
            }

            @Override
            public void onGalleryClickItem(int i, View view) {
                if (SketchListActivity.this.isWriteStoragePermissionGranted()) {
                    ImagePicker.with(SketchListActivity.this).galleryOnly().start();
                }


            }
        };
        this.drawing_adapter = drawingListAdapter;
        this.rv_drawing_list.setAdapter(drawingListAdapter);
    }


    public ArrayList<String> addAssetsImages(String str) {
        String[] list;
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            arrayList.add("Gallery");
            for (String str2 : getAssets().list(str)) {
                arrayList.add(str + File.separator + str2);
                Log.e("pathList item", str + File.separator + str2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public void HelpScreen() {
        startActivity(new Intent(this, HelpActivity.class));
    }

    public void HelpScreen2() {
        startActivity(new Intent(this, HelpActivity2.class));
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 == -1) {
            this.SelectedImagePath = AppConstant.getRealPathFromURI_API19(this, intent.getData());
            if (AppConstant.selected_id.equals(AppConstant.TraceDirect)) {
                this.action_name = AppConstant.TraceDirect;
            } else {
                this.action_name = AppConstant.TracePaper;
            }
            if (AppConstant.selected_id.equals(AppConstant.TraceDirect)) {
                TraceDrawingScreen();
            } else {
                TracePaperScreen();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.action_name = this.BACK;

        BackScreen();
    }


    private void BackScreen() {

        finish();
        AppConstants.overridePendingTransitionExit(this);
    }

    public void TracePaperScreen() {
        Intent intent = new Intent(this, TracePaperActivity.class);
        intent.putExtra("ImagePath", this.SelectedImagePath);
        startActivity(intent);
    }

    public void TraceDrawingScreen() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("ImagePath", this.SelectedImagePath);
        startActivity(intent);
    }
}
