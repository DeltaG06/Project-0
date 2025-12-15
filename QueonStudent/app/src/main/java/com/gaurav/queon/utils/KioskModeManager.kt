package com.gaurav.queon.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.gaurav.queon.QueonDeviceAdminReceiver

object KioskModeManager {
    
    private const val REQUEST_CODE_ENABLE_ADMIN = 1001
    
    fun isDeviceAdminEnabled(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, QueonDeviceAdminReceiver::class.java)
        return dpm.isAdminActive(adminComponent)
    }
    
    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }
    
    fun requestDeviceAdmin(activity: Activity) {
        val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(activity, QueonDeviceAdminReceiver::class.java)
        
        if (!dpm.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Queon needs Device Admin permission to enable secure exam mode and prevent cheating."
                )
            }
            activity.startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
        }
    }
    
    fun startKioskMode(activity: Activity): Boolean {
        return try {
            val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(activity, QueonDeviceAdminReceiver::class.java)
            
            if (dpm.isDeviceOwnerApp(activity.packageName)) {
                // Device owner - can use full lock task mode
                dpm.setLockTaskPackages(adminComponent, arrayOf(activity.packageName))
                activity.startLockTask()
                true
            } else if (dpm.isAdminActive(adminComponent)) {
                // Device admin but not owner - use regular lock task
                activity.startLockTask()
                true
            } else {
                // No admin - try anyway (will only work if user enables screen pinning)
                activity.startLockTask()
                false // Indicate it might not work
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun stopKioskMode(activity: Activity) {
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    fun getKioskModeStatus(context: Context): String {
        return when {
            isDeviceOwner(context) -> "Full Kiosk Mode (Device Owner)"
            isDeviceAdminEnabled(context) -> "Enhanced Mode (Device Admin)"
            else -> "Basic Mode (Manual Pinning Required)"
        }
    }
}
