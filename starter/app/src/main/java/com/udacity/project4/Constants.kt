package com.udacity.project4

import java.util.concurrent.TimeUnit

object Constants {
    const val DEFAULT_LAT = 37.422
    const val DEFAULT_LNG = -122.08
    const val GEOFENCE_RADIUS_IN_METERS = 1000f
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    const val ACTION_GEOFENCE_EVENT = "SaveReminderFragment.project4.action.ACTION_GEOFENCE_EVENT"
    val GEOFENCE_EXPIRE_TIME = TimeUnit.DAYS.toMillis(30)
}