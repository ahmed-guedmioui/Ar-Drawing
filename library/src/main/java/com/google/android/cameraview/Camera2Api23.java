package com.google.android.cameraview;

import android.content.Context;
import android.hardware.camera2.params.StreamConfigurationMap;


class Camera2Api23 extends Camera2 {
    /* JADX INFO: Access modifiers changed from: package-private */
    public Camera2Api23(PreviewImpl previewImpl, Context context) {
        super(previewImpl, context);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.android.cameraview.Camera2
    public void collectPictureSizes(SizeMap sizeMap, StreamConfigurationMap streamConfigurationMap) {
        android.util.Size[] highResolutionOutputSizes;
        if (streamConfigurationMap.getHighResolutionOutputSizes(256) != null) {
            for (android.util.Size size : streamConfigurationMap.getHighResolutionOutputSizes(256)) {
                sizeMap.add(new Size(size.getWidth(), size.getHeight()));
            }
        }
        if (sizeMap.isEmpty()) {
            super.collectPictureSizes(sizeMap, streamConfigurationMap);
        }
    }
}
