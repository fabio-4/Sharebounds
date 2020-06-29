package com.sharebounds.sharebounds.camera;

import android.graphics.Matrix;
import android.hardware.Camera;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CameraUtils {

    static String editString(String text, ImageGestures.GestureType type) {
        text = text.replaceAll("\n", " ");
        if (type == ImageGestures.GestureType.LONG_PRESS) text += "\n";
        else text += " ";
        return text;
    }

    static Matrix fixRotationMatrix(float fixRotation, int imgW, int imgH) {
        Matrix matrix = new Matrix();
        matrix.postRotate(fixRotation);
        if (fixRotation == 90) matrix.postTranslate(imgW, 0);
        else if (fixRotation == -90 || fixRotation == 270) matrix.postTranslate(0, imgH);
        else if (fixRotation == 180) matrix.postTranslate(imgW, imgH);
        return matrix;
    }

    static <T> List<T> intersection(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<>();
        for (T t : list1) {
            if (list2.contains(t)) {
                set.add(t);
            }
        }
        return new ArrayList<>(set);
    }

    static Camera.Size chooseOptimalSize(List<Camera.Size> outputSizes, int width, int height) {
        int MAX = 1920;
        int targetW = Math.min(width, MAX);
        int targetH = Math.min(height, MAX);

        Camera.Size currentOptimalSize = null;
        double currentOptimalDiffW = Double.MAX_VALUE;
        double currentOptimalDiffH = Double.MAX_VALUE;
        Camera.Size backupSize = null;
        double currentBackupDiff = Double.MAX_VALUE;

        for (Camera.Size currentSize : outputSizes) {
            if (currentSize.width > MAX || currentSize.height > MAX) continue;
            double currentDiffW = currentSize.width - targetW;
            double currentDiffH = currentSize.height - targetH;
            if (((Math.abs(currentDiffW) < currentOptimalDiffW) && currentDiffH > 0)
                    || ((Math.abs(currentDiffH) < currentOptimalDiffH) && currentDiffW > 0)) {
                currentOptimalSize = currentSize;
                currentOptimalDiffW = Math.abs(currentDiffW);
                currentOptimalDiffH = Math.abs(currentDiffH);
            } else if ((currentOptimalSize == null) &&
                    (Math.abs(currentDiffW) + Math.abs(currentDiffH) < currentBackupDiff)) {
                backupSize = currentSize;
                currentBackupDiff = Math.abs(currentDiffW) + Math.abs(currentDiffH);
            }
        }

        if (currentOptimalSize == null) {
            if (backupSize != null) currentOptimalSize = backupSize;
            else currentOptimalSize = outputSizes.get(0);
        }
        return currentOptimalSize;
    }
}
