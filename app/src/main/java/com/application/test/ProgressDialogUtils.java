package com.application.test;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;


public class ProgressDialogUtils {

    private static ProgressDialog progressDialog;

    public ProgressDialogUtils() {
        throw new Error("U will not able to instantiate it");
    }


    public static void show(Context context) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        int style;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            style = android.R.style.Theme_Material_Light_Dialog;
        } else {
            //noinspection deprecation
            style = ProgressDialog.THEME_HOLO_LIGHT;
        }

        progressDialog = new ProgressDialog(context, style);
        progressDialog.setMessage(context.getResources().getString(R.string.please_wait_));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void dismiss() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public static boolean isVisible() {
        return progressDialog != null && progressDialog.isShowing();
    }
}
