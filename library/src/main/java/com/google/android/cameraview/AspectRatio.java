package com.google.android.cameraview;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.collection.SparseArrayCompat;


public class AspectRatio implements Comparable<AspectRatio>, Parcelable {
    private final int mX;
    private final int mY;
    private static final SparseArrayCompat<SparseArrayCompat<AspectRatio>> sCache = new SparseArrayCompat<>(16);
    public static final Parcelable.Creator<AspectRatio> CREATOR = new Parcelable.Creator<AspectRatio>() { // from class: com.google.android.cameraview.AspectRatio.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AspectRatio createFromParcel(Parcel parcel) {
            return AspectRatio.of(parcel.readInt(), parcel.readInt());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AspectRatio[] newArray(int i) {
            return new AspectRatio[i];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static AspectRatio of(int i, int i2) {
        int gcd = gcd(i, i2);
        int i3 = i / gcd;
        int i4 = i2 / gcd;
        SparseArrayCompat<SparseArrayCompat<AspectRatio>> sparseArrayCompat = sCache;
        SparseArrayCompat<AspectRatio> sparseArrayCompat2 = sparseArrayCompat.get(i3);
        if (sparseArrayCompat2 == null) {
            AspectRatio aspectRatio = new AspectRatio(i3, i4);
            SparseArrayCompat<AspectRatio> sparseArrayCompat3 = new SparseArrayCompat<>();
            sparseArrayCompat3.put(i4, aspectRatio);
            sparseArrayCompat.put(i3, sparseArrayCompat3);
            return aspectRatio;
        }
        AspectRatio aspectRatio2 = sparseArrayCompat2.get(i4);
        if (aspectRatio2 == null) {
            AspectRatio aspectRatio3 = new AspectRatio(i3, i4);
            sparseArrayCompat2.put(i4, aspectRatio3);
            return aspectRatio3;
        }
        return aspectRatio2;
    }

    public static AspectRatio parse(String str) {
        int indexOf = str.indexOf(58);
        if (indexOf == -1) {
            throw new IllegalArgumentException("Malformed aspect ratio: " + str);
        }
        try {
            return of(Integer.parseInt(str.substring(0, indexOf)), Integer.parseInt(str.substring(indexOf + 1)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Malformed aspect ratio: " + str, e);
        }
    }

    private AspectRatio(int i, int i2) {
        this.mX = i;
        this.mY = i2;
    }

    public int getX() {
        return this.mX;
    }

    public int getY() {
        return this.mY;
    }

    public boolean matches(Size size) {
        int gcd = gcd(size.getWidth(), size.getHeight());
        return this.mX == size.getWidth() / gcd && this.mY == size.getHeight() / gcd;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof AspectRatio) {
            AspectRatio aspectRatio = (AspectRatio) obj;
            return this.mX == aspectRatio.mX && this.mY == aspectRatio.mY;
        }
        return false;
    }

    public String toString() {
        return this.mX + ":" + this.mY;
    }

    public float toFloat() {
        return this.mX / this.mY;
    }

    public int hashCode() {
        int i = this.mY;
        int i2 = this.mX;
        return i ^ ((i2 >>> 16) | (i2 << 16));
    }

    @Override // java.lang.Comparable
    public int compareTo(AspectRatio aspectRatio) {
        if (equals(aspectRatio)) {
            return 0;
        }
        return toFloat() - aspectRatio.toFloat() > 0.0f ? 1 : -1;
    }

    public AspectRatio inverse() {
        return of(this.mY, this.mX);
    }

    private static int gcd(int i, int i2) {
        while (true) {
            int i3 = i2;
            int i4 = i;
            i = i3;
            if (i == 0) {
                return i4;
            }
            i2 = i4 % i;
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mX);
        parcel.writeInt(this.mY);
    }
}
