package com.google.android.cameraview;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import java.nio.ByteBuffer;


public class Utils {
    public static byte[] YUV420toNV21(Image image) {
        int i;
        Rect cropRect = image.getCropRect();
        int format = image.getFormat();
        int width = cropRect.width();
        int height = cropRect.height();
        Image.Plane[] planes = image.getPlanes();
        int i2 = width * height;
        byte[] bArr = new byte[(ImageFormat.getBitsPerPixel(format) * i2) / 8];
        byte[] bArr2 = new byte[planes[0].getRowStride()];
        int i3 = 1;
        int i4 = 0;
        int i5 = 0;
        int i6 = 1;
        while (i4 < planes.length) {
            if (i4 != 0) {
                if (i4 == i3) {
                    i5 = i2 + 1;
                } else if (i4 == 2) {
                    i5 = i2;
                }
                i6 = 2;
            } else {
                i5 = 0;
                i6 = 1;
            }
            ByteBuffer buffer = planes[i4].getBuffer();
            int rowStride = planes[i4].getRowStride();
            int pixelStride = planes[i4].getPixelStride();
            int i7 = i4 == 0 ? 0 : 1;
            int i8 = width >> i7;
            int i9 = height >> i7;
            int i10 = width;
            int i11 = height;
            buffer.position(((cropRect.top >> i7) * rowStride) + ((cropRect.left >> i7) * pixelStride));
            for (int i12 = 0; i12 < i9; i12++) {
                if (pixelStride == 1 && i6 == 1) {
                    buffer.get(bArr, i5, i8);
                    i5 += i8;
                    i = i8;
                } else {
                    i = ((i8 - 1) * pixelStride) + 1;
                    buffer.get(bArr2, 0, i);
                    for (int i13 = 0; i13 < i8; i13++) {
                        bArr[i5] = bArr2[i13 * pixelStride];
                        i5 += i6;
                    }
                }
                if (i12 < i9 - 1) {
                    buffer.position((buffer.position() + rowStride) - i);
                }
            }
            i4++;
            width = i10;
            height = i11;
            i3 = 1;
        }
        return bArr;
    }
}
