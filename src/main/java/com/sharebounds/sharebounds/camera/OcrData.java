package com.sharebounds.sharebounds.camera;

import android.graphics.Rect;
import android.graphics.RectF;

class OcrData {

    final String text;
    final RectF boundingBox;

    OcrData(String text, Rect boundingBox) {
        this.text = text;
        this.boundingBox = new RectF(boundingBox);
    }
}
