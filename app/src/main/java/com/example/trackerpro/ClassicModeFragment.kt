package com.example.trackerpro

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.fragment_classic_mode.*
import java.util.*


class ClassicModeFragment : Fragment() {


    private var secondsAfterLocationWasFound: Int = 0
    private var locationWasChanged: Boolean = false
    private var running: Boolean = false
    private var isFinish: Boolean = false
    private var isNewSession = false
    private var currentLocationPoint: LocationPoint? = null
    private var trackerAnalyzer: TrackerAnalyzer? = null

    private var seconds: Int = 0
    private var distance: Float = 0f
    private var speed: Float = 0f
    private var calories: Float = 0f
    private lateinit var formOfActivity: FormOfActivity

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var textViewDistance: TextView
    private lateinit var textViewSpeed: TextView
    private lateinit var textViewAverageSpeed: TextView
    private lateinit var textViewCalories: TextView
    private lateinit var radioButtonWalk: RadioButton
    private lateinit var radioButtonRun: RadioButton
    private lateinit var radioButtonBicycle: RadioButton


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trackerAnalyzer = TrackerAnalyzer(context!!)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)

        isNewSession = true

        getLocationUpdates()
        startLocationUpdates()
        runTimer()
    }

    private fun getLocationUpdates() {

        locationRequest = LocationRequest()
        locationRequest.interval = 3000
        locationRequest.fastestInterval = 2000
        locationRequest.smallestDisplacement = 2f
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    val location =
                            locationResult.lastLocation

                    var newLocationPoint: LocationPoint = LocationPoint(location, seconds)

                    if (currentLocationPoint==null) {
                        currentLocationPoint = newLocationPoint
                        Log.e("getLocationUpdates: startlocation - ", location.latitude.toString() + " " + location.longitude.toString())
                    }else {

                        if (running){
                            trackerAnalyzer?.setPoints(currentLocationPoint!!, newLocationPoint)
                            trackerAnalyzer?.analyze()

                            secondsAfterLocationWasFound = seconds
                            distance += trackerAnalyzer!!.distance

                            val distanceString: String = java.lang.String
                                    .format(
                                            Locale.getDefault(),
                                            "%.0f m", distance
                                    )

                            val speedString: String = java.lang.String
                                    .format(
                                            Locale.getDefault(),
                                            "%.1f km/h", convertSpeedFromMSecToKmH(trackerAnalyzer!!.speed)
                                    )

                            speed = trackerAnalyzer!!.speed

                            textViewDistance.text = distanceString
                            textViewSpeed.text = speedString

                            locationWasChanged = true

                            Log.e("Distance",distanceString )
                            Log.e("Speed",speedString )
                        }

                        currentLocationPoint = newLocationPoint

                        Log.e("getLocationUpdates: secondlocation - ", location.latitude.toString() + " " + location.longitude.toString())

                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(context!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)

            Log.e("startLocationUpdates", "it doesn't go")

            return
        }

        Log.e("startLocationUpdates", "it goes")

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun displayValuesOfTracking() {

        if (seconds - secondsAfterLocationWasFound >= 8) {

            val speedString: String = java.lang.String
                    .format(
                            Locale.getDefault(),
                            "%.1f km/h", 0f
                    )

            Log.e("Speed", speedString)

            textViewSpeed.text = speedString
            speed = 0f
        }

        val averageSpeed: String = java.lang.String
                .format(
                        Locale.getDefault(),
                        "%.1f km/h", convertSpeedFromMSecToKmH((distance / seconds).toFloat())
                )

        textViewAverageSpeed.text = averageSpeed

        var caloriesTemp: Float = 0f

        if (formOfActivity == FormOfActivity.WALK) {
            caloriesTemp = calculateCaloriesWalking(speed)
            calories += caloriesTemp

        } else if (formOfActivity == FormOfActivity.RUN) {
            caloriesTemp = calculateCaloriesRunning(distance)
            calories = caloriesTemp

        } else if (formOfActivity == FormOfActivity.BICYCLE){
            caloriesTemp = calculateCaloriesBicycle(seconds)
            calories = caloriesTemp
        }

        textViewCalories.text = java.lang.String
                .format(
                        Locale.getDefault(),
                        "%.0f kcal", calories
                )
    }

    private fun runTimer() {

        val handler = Handler()

        handler.post(object : Runnable {
            override fun run() {

                val hours = seconds / 3600
                val minutes = seconds % 3600 / 60
                val secs = seconds % 60

                val time: String = java.lang.String
                    .format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d", hours,
                        minutes, secs
                    )

                if (textViewStopwatch != null) {
                    textViewStopwatch.text = time
                }

                if (running) {
                    seconds++
                    displayValuesOfTracking()
                }

                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View =  inflater.inflate(R.layout.fragment_classic_mode, container, false)

        val buttonStartTracking = view.findViewById<Button>(R.id.buttonStartTrackingId)
        val buttonStopTracking = view.findViewById<Button>(R.id.buttonStopTrackingId)
        val textViewStopWatch = view.findViewById<TextView>(R.id.textViewStopwatch)

        textViewDistance = view.findViewById(R.id.textViewDistanceCounter)
        textViewSpeed = view.findViewById(R.id.textViewSpeedCounter)
        textViewAverageSpeed = view.findViewById(R.id.textViewAverageSpeedCounter)
        textViewCalories = view.findViewById(R.id.textViewCaloriesCounter)
        radioButtonWalk = view.findViewById(R.id.radioButtonWalkDistanceMode)
        radioButtonRun = view.findViewById(R.id.radioButtonRunDistanceMode)
        radioButtonBicycle = view.findViewById(R.id.radioButtonBicycleDistanceMode)

        if (running) {

            val hours = seconds / 3600
            val minutes = seconds % 3600 / 60
            val secs = seconds % 60

            val time: String = java.lang.String
                    .format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d", hours,
                        minutes, secs
                    )

            textViewStopWatch.text = time

            buttonStartTracking.text = getString(R.string.pause_tracking)
            buttonStopTracking.isEnabled = true
        }

        buttonStartTracking.setOnClickListener {

            if (isNewSession){
                disactivateRadioButtons()
                setFormOfActivity()
            }

            isNewSession = false

            if (running){
                running = false
                buttonStartTracking.text = getString(R.string.continue_tracking)

            } else {

                if (isFinish) {
                    isFinish = false
                    textViewStopWatch.text = getString(R.string.time_default)
                    resetValues()

                    val distanceString: String = java.lang.String
                            .format(
                                    Locale.getDefault(),
                                    "%.0f m", 0f
                            )

                    textViewDistance.text = distanceString

                    val speedString: String = java.lang.String
                            .format(
                                    Locale.getDefault(),
                                    "%.1f km/h", 0f
                            )

                    textViewSpeed.text = speedString

                }

                running = true
                buttonStartTracking.text = getString(R.string.pause_tracking)
                buttonStopTracking.isEnabled = true
            }
        }

        buttonStopTracking.setOnClickListener {

            running = false
            isFinish = true
            isNewSession = true

            activateRadioButtons()

            buttonStartTracking.text = getString(R.string.start_tracking)
            buttonStopTracking.isEnabled = false
        }

        return view
    }

    override fun onStop() {
        super.onStop()
    }

    fun convertSpeedFromKmHToMSec(speedPar: Float): Float {
        return speedPar * 1000 / 3600
    }

    fun convertSpeedFromMSecToKmH(speedPar: Float): Float {
        return speedPar / 1000 * 3600
    }

    fun calculateCaloriesWalking(speedInSec: Float): Float {
        val caloriesTemp: Float = if (speedInSec == 0f) 0f else ((0.035 * 60 + (speedInSec * speedInSec / 1.7) * 0.029 * 60)/60).toFloat()
        return caloriesTemp
    }

    fun calculateCaloriesRunning(distancePar: Float): Float {
        val caloriesTemp: Float = if (distancePar == 0f) 0f else (distancePar * 0.06).toFloat()
        return caloriesTemp
    }

    fun calculateCaloriesBicycle(timePar: Int): Float {
        val caloriesTemp: Float = if (timePar == 0) 0f else (0.175 * timePar).toFloat()
        return caloriesTemp
    }

    fun resetValues() {
        seconds = 0
        distance = 0f
        calories = 0f
        speed = 0f
    }

    fun disactivateRadioButtons() {
        radioButtonWalk.isClickable = false
        radioButtonRun.isClickable = false
        radioButtonBicycle.isClickable = false
    }

    fun activateRadioButtons() {
        radioButtonWalk.isClickable = true
        radioButtonRun.isClickable = true
        radioButtonBicycle.isClickable = true
    }

    fun setFormOfActivity() {

        if (radioButtonWalk.isChecked) {
            formOfActivity = FormOfActivity.WALK

        } else if (radioButtonRun.isChecked) {
            formOfActivity = FormOfActivity.RUN

        } else if (radioButtonBicycle.isChecked) {
            formOfActivity = FormOfActivity.BICYCLE
        }
    }

}