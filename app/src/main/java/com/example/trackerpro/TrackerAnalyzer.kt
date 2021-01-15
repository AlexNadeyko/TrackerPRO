package com.example.trackerpro

import android.content.Context


class TrackerAnalyzer constructor(var context: Context) {

    lateinit var oldLocationPoint: LocationPoint
    lateinit var newLocationPoint: LocationPoint

    var distance: Float = 0f
    var time: Int = 0
    var speed: Float = 0f

    fun setPoints(oldPoint: LocationPoint, newPoint: LocationPoint) {
        oldLocationPoint = oldPoint
        newLocationPoint = newPoint
    }

    fun analyze() {
        distance = findDistanceBetween()
        time = findTimeBetween()
        speed = findSpeedBetween(distance, time)
    }

    fun findDistanceBetween(): Float {
        var distance: Float = 0f
        distance = oldLocationPoint.location.distanceTo(newLocationPoint.location)

        return distance
    }

    fun findTimeBetween(): Int {
        return newLocationPoint.time - oldLocationPoint.time
    }

    fun findSpeedBetween(distance: Float, time: Int): Float {
        var speed: Float = 0f

        if(time == 0) {
            speed = 0f
        } else {
            speed = (distance / time).toFloat()
        }

        return speed
    }

}