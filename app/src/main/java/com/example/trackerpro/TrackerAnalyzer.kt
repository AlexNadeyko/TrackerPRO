package com.example.trackerpro

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import com.example.trackerpro.ClassicModeFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class TrackerAnalyzer constructor(var context: Context) {

    lateinit var oldLocationPoint: LocationPoint
    lateinit var newLocationPoint: LocationPoint

    var distance: Float = 0f
    var time: Int = 0
    var speed: Float = 0f

    fun setPoints(oldPoint: LocationPoint, newPoint: LocationPoint){
        oldLocationPoint = oldPoint
        newLocationPoint = newPoint
    }

    fun analyze(){
        distance = findDistanceBetween()
        time = findTimeBetween()
        speed = findSpeedBetween(distance, time)

    }

    fun findDistanceBetween(): Float{
        var distance: Float = 0f

        distance = oldLocationPoint.location.distanceTo(newLocationPoint.location)
//        Toast.makeText(context, "distance -$distance", Toast.LENGTH_LONG).show()

        return distance
    }

    fun findTimeBetween(): Int{
        return newLocationPoint.time-oldLocationPoint.time
    }

    fun findSpeedBetween(distance: Float, time: Int): Float {
        var speed: Float = 0f
        if(time == 0) {
            speed = 0f
        }else {
            speed = (distance / time).toFloat()
        }
        return speed
    }
}