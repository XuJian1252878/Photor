package com.example.photopicker.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.example.photopicker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/1/8.
 */

public class PermissionsUtils {

    public static boolean checkReadStoragePermission(Activity activity) {

        // 读取权限是在android 4之后加入的
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return true;
        }
        int readStoragePermissionState = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean readStoragePermissionGranted = readStoragePermissionState == PackageManager.PERMISSION_GRANTED;

        if (!readStoragePermissionGranted) {
            requestPermission(activity,
                    PermissionsConstant.PERMISSIONS_EXTERNAL_READ,
                    activity.getString(R.string.pick_permission_rationale),
                    PermissionsConstant.REQUEST_EXTERNAL_READ);
        }
        return readStoragePermissionGranted;
    }

    public static boolean checkWriteStoragePermission(Fragment fragment) {
        int writeStoragePermissionState = ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean writeStoragePermissionGranted = writeStoragePermissionState == PackageManager.PERMISSION_GRANTED;

        if (!writeStoragePermissionGranted) {
            requestPermission(fragment.getActivity(), PermissionsConstant.PERMISSIONS_EXTERNAL_WRITE,
                    fragment.getString(R.string.pick_permission_rationale_write_storage),
                    PermissionsConstant.REQUEST_EXTERNAL_WRITE);
        }
        return writeStoragePermissionGranted;
    }

    public static boolean checkCameraPermission(Fragment fragment) {
        int cameraPermissionState = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.CAMERA);
        boolean cameraPermissionGranted = cameraPermissionState == PackageManager.PERMISSION_GRANTED;

        if (!cameraPermissionGranted) {
            requestPermission(fragment.getActivity(),
                    PermissionsConstant.PERMISSIONS_CAMERA,
                    fragment.getString(R.string.pick_permission_rationale),
                    PermissionsConstant.REQUEST_CAMERA);
        }

        return cameraPermissionGranted;
    }

    private static void requestPermission(final Activity activity, final String[] permissions, String rationale, final int requestCode) {
        String[] resultPermissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final List<String> permissionsToRequest = new ArrayList<>();
            for (final String permission: permissions) {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.pick_permission_dialog_title)
                            .setMessage(rationale)
                            .setPositiveButton(R.string.pick_permission_dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    permissionsToRequest.add(permission);
                                }
                            })
                            .setNegativeButton(R.string.pick_permission_dialog_cancel, null)
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
