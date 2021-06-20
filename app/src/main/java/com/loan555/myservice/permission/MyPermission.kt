package com.loan555.myservice.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val STORAGE_REQUEST_CODE = 1

class MyPermission(private val context: Context, private val activity: Activity) {

    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun checkStoragePermission(): Boolean {
        Log.e("permission", "check permission")
        return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
    }

    fun requestStoragePermission() {
        ActivityCompat.requestPermissions(activity, storagePermission, STORAGE_REQUEST_CODE)
    }
}