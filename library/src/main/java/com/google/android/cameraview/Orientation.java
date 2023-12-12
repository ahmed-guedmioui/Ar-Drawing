package com.google.android.cameraview;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.WindowManager;



public class Orientation implements SensorEventListener {
    private int mLastAccuracy;
    private Listener mListener;
    private Sensor mRotationSensor;
    private final SensorManager mSensorManager;
    private WindowManager mWindowManager;
    private int sensorInterval;

    
    public interface Listener {
        void onOrientationChanged(float f, float f2);
    }

    public Orientation(Activity activity, int i) {
        this.sensorInterval = 50;
        this.mWindowManager = activity.getWindow().getWindowManager();
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        this.mSensorManager = sensorManager;
        if (sensorManager != null) {
            this.mRotationSensor = sensorManager.getDefaultSensor(11);
        }
        this.sensorInterval = i;
    }

    public Orientation(Context context, int i) {
        this.sensorInterval = 50;
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.mSensorManager = sensorManager;
        if (sensorManager != null) {
            this.mRotationSensor = sensorManager.getDefaultSensor(11);
        }
        this.sensorInterval = i;
    }

    public void startListening(Listener listener) {
        if (this.mListener == listener) {
            return;
        }
        this.mListener = listener;
        Sensor sensor = this.mRotationSensor;
        if (sensor == null) {
            Log.w("TAG_ORIENTATION", "Rotation vector sensor not available; will not provide orientation data.");
        } else {
            this.mSensorManager.registerListener(this, sensor, this.sensorInterval);
        }
    }

    public void stopListening() {
        this.mSensorManager.unregisterListener(this);
        this.mListener = null;
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int i) {
        if (this.mLastAccuracy != i) {
            this.mLastAccuracy = i;
        }
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (this.mListener == null || this.mLastAccuracy == 0 || sensorEvent.sensor != this.mRotationSensor) {
            return;
        }
        updateOrientation(sensorEvent.values);
    }

    private void updateOrientation(float[] fArr) {
        float[] fArr2 = new float[9];
        SensorManager.getRotationMatrixFromVector(fArr2, fArr);
        WindowManager windowManager = this.mWindowManager;
        int i = 131;
        int i2 = 129;
        if (windowManager != null) {
            int rotation = windowManager.getDefaultDisplay().getRotation();
            if (rotation == 1) {
                i = 3;
            } else if (rotation == 2) {
                i = 129;
                i2 = 131;
            } else if (rotation == 3) {
                i2 = 1;
            }
            float[] fArr3 = new float[9];
            SensorManager.remapCoordinateSystem(fArr2, i, i2, fArr3);
            float[] fArr4 = new float[3];
            SensorManager.getOrientation(fArr3, fArr4);
            this.mListener.onOrientationChanged(fArr4[1] * (-57.0f), fArr4[2] * (-57.0f));
        }
        i = 1;
        i2 = 3;
        float[] fArr32 = new float[9];
        SensorManager.remapCoordinateSystem(fArr2, i, i2, fArr32);
        float[] fArr42 = new float[3];
        SensorManager.getOrientation(fArr32, fArr42);
        this.mListener.onOrientationChanged(fArr42[1] * (-57.0f), fArr42[2] * (-57.0f));
    }
}
