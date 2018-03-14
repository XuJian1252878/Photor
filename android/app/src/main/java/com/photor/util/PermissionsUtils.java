package com.photor.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.photor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/3/13.
 */

public class PermissionsUtils {

    public static boolean checkWriteStoragePermission(Activity activity, int requestCode) {
        boolean writeStoragePermissionGranted = checkSelfPermission(activity, PermissionsConstant.PERMISSIONS_EXTERNAL_WRITE);
        if (!writeStoragePermissionGranted) {
            requestPermission(activity,
                    PermissionsConstant.PERMISSIONS_EXTERNAL_WRITE,
                    activity.getString(R.string.permission_write_storage),
                    requestCode);
        }

        return writeStoragePermissionGranted;
    }


    public static boolean checkCameraPermission(Fragment fragment, int requestCode) {
        boolean cameraPermissionGranted = checkSelfPermission(fragment.getContext(), PermissionsConstant.PERMISSIONS_CAMERA);

        if (!cameraPermissionGranted) {
            requestPermission(fragment.getActivity(),
                    PermissionsConstant.PERMISSIONS_CAMERA,
                    fragment.getString(R.string.permission_camera),
                    requestCode);
        }

        return cameraPermissionGranted;
    }

    private static boolean checkSelfPermission(Context context, String[] permissions) {
        boolean flag = true;

        for (String permission : permissions) {
            flag &= (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
        }

        return flag;
    }

    private static void requestPermission(final Activity activity, final String[] permissions, String rationale, final int requestCode) {
        String[] resultPermissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final List<String> permissionsToRequest = new ArrayList<>();
            for (final String permission: permissions) {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    new AlertDialog.Builder(activity)
                            .setTitle(com.example.photopicker.R.string.pick_permission_dialog_title)
                            .setMessage(rationale)
                            .setPositiveButton(com.example.photopicker.R.string.pick_permission_dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    permissionsToRequest.add(permission);
                                }
                            })
                            .setNegativeButton(com.example.photopicker.R.string.pick_permission_dialog_cancel, null)
                            .create()
                            .show();
                } else {
                    permissionsToRequest.add(permission);
                }
            }
            resultPermissions = permissionsToRequest.toArray(new String[permissionsToRequest.size()]);
        } else {
            resultPermissions = permissions;
        }
        ActivityCompat.requestPermissions(activity, resultPermissions, requestCode);
    }

}
