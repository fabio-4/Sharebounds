package com.sharebounds.sharebounds.camera;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageData implements Parcelable {

    public Uri uri;
    float rotation;
    float[] matrixValues;
    float fixRotation = 0;
    boolean portrait = true;

    public ImageData(Uri uri, float rotation, float[] matrixValues) {
        this.uri = uri;
        this.rotation = rotation;
        this.matrixValues = matrixValues;
    }

    private ImageData(Parcel in){
        String uriString = in.readString();
        if (uriString != null) this.uri = Uri.parse(uriString);
        this.rotation = in.readFloat();
        int length = in.readInt();
        if (length != -1) {
            matrixValues = new float[length];
            in.readFloatArray(matrixValues);
        }
        portrait = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        String uriString = null;
        if (uri != null) uriString = uri.toString();
        parcel.writeString(uriString);
        parcel.writeFloat(rotation);
        if (matrixValues != null) {
            parcel.writeInt(matrixValues.length);
            parcel.writeFloatArray(matrixValues);
        } else parcel.writeInt(-1);
        parcel.writeInt(portrait ? 1 : 0);
    }

    public static final Parcelable.Creator<ImageData> CREATOR = new Parcelable.Creator<ImageData>() {
        public ImageData createFromParcel(Parcel in) {
            return new ImageData(in);
        }

        public ImageData[] newArray(int size) {
            return new ImageData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
