package com.sharebounds.sharebounds;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class PermissionUtils {

    public enum Type {
        Camera (Manifest.permission.CAMERA),
        Storage (Manifest.permission.READ_EXTERNAL_STORAGE);

        private final String sPermission;
        Type(String permission) {
            sPermission = permission;
        }

        String getPermissionType() {
            return sPermission;
        }

        static int getRequestCode(Type permission) {
            return (permission == Type.Camera) ? CAMERA_REQUEST : STORAGE_REQUEST;
        }

        public static Type getType(int requestCode) {
            return (requestCode == CAMERA_REQUEST) ? Type.Camera : Type.Storage;
        }
    }

    public static final int CAMERA_REQUEST = 0;
    public static final int STORAGE_REQUEST = 1;

    public static boolean getPermission(final Context context, Type permission) {
        return (ContextCompat.checkSelfPermission(context, permission.getPermissionType())
                == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestPermission(Activity activity, Type permission) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission.getPermissionType()},
                Type.getRequestCode(permission));
    }

    public static void errorToast(Context context, Type permission) {
        String error = (permission == Type.Camera) ?
                context.getString(R.string.camera_permission_denied)
                : context.getString(R.string.storage_permission_denied);
        Toast.makeText(context, error, Toast.LENGTH_LONG).show();
    }
}
