package com.example.trackerpro

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.*


class DistanceModeFragment : Fragment() {

    private var secondsAfterLocationWasFound: Int = 0
    private var running: Boolean = false
    private var isFinish: Boolean = false
    private var isNewSession = false
    private var locationWasChanged: Boolean = false

    private var currentLocationPoint: LocationPoint? = null
    private var trackerAnalyzer: TrackerAnalyzer? = null

    private var seconds: Int = 0
    private var distance: Float = 0f
    private var speed: Float = 0f
    private var calories: Float = 0f
    private var distanceToGo: Float = 0f
    private var distanceToGoSet = 0f
    private lateinit var formOfActivity: FormOfActivity
    private var distanceEnd: Boolean = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var editTextViewDistance: EditText
    private lateinit var textViewSpeed: TextView
    private lateinit var textViewAverageSpeed: TextView
    private lateinit var textViewCalories: TextView
    private lateinit var textViewTime: TextView
    private lateinit var radioButtonWalk: RadioButton
    private lateinit var radioButtonRun: RadioButton
    private lateinit var radioButtonBicycle: RadioButton
    private lateinit var progressBarDistance: ProgressBar
    private lateinit var buttonStartTracking: Button
    private lateinit var buttonStopTracking: Button


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

                    } else {

                        if (running) {
                            trackerAnalyzer?.setPoints(currentLocationPoint!!, newLocationPoint)
                            trackerAnalyzer?.analyze()

                            secondsAfterLocationWasFound = seconds
                            distance += trackerAnalyzer!!.distance

                            val speedString: String = java.lang.String
                                    .format(
                                            Locale.getDefault(),
                                            "%.1f km/h", convertSpeedFromMSecToKmH(trackerAnalyzer!!.speed)
                                    )

                            speed = trackerAnalyzer!!.speed

                            distanceToGo = distanceToGoSet - distance

                            updateDistanceProgressBar()

                            if (distanceToGo < 0) {
                                distanceToGo = 0f
                                distanceEnd = true
                            }

                            val distanceToGoString: String = java.lang.String
                                    .format(
                                            Locale.getDefault(),
                                            "%.0f m", distanceToGo
                                    )

                            editTextViewDistance.setText(distanceToGoString)
                            textViewSpeed.text = speedString

                            locationWasChanged = true

                            if (distanceEnd) {
                                stopTracking()
                            }

                            Log.e("Distance",distance.toString() )
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
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), DistanceModeFragment.LOCATION_PERMISSION_REQUEST_CODE)

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

    private fun displayValuesOfTracking(){

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

        }else if(formOfActivity == FormOfActivity.RUN) {
            caloriesTemp = calculateCaloriesRunning(distance)
            calories = caloriesTemp

        } else if(formOfActivity == FormOfActivity.BICYCLE) {
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

                if (textViewTime != null) {
                    textViewTime.text = time
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View =  inflater.inflate(R.layout.fragment_distance_mode, container, false)

        buttonStartTracking = view.findViewById<Button>(R.id.buttonStartTrackingDistanceMode)
        buttonStopTracking = view.findViewById<Button>(R.id.buttonStopTrackingDistanceMode)

        progressBarDistance = view.findViewById(R.id.progressBarDistance)
        editTextViewDistance = view.findViewById(R.id.editTextNumberDistance)
        textViewTime = view.findViewById(R.id.textViewTimeCounterDistanceMode)
        textViewSpeed = view.findViewById(R.id.textViewSpeedCounterDistanceMode)
        textViewAverageSpeed = view.findViewById(R.id.textViewAverageSpeedCounterDistanceMode)
        textViewCalories = view.findViewById(R.id.textViewCaloriesCounterDistanceMode)
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

            textViewTime.text = time

            buttonStartTracking.text = getString(R.string.pause_tracking)
            buttonStopTracking.isEnabled = true
        }

        buttonStartTracking.setOnClickListener {

            if ((editTextViewDistance.text).toString() == "") {
                editTextViewDistance.requestFocus()
            } else {

                if (isNewSession) {

                    progressBarDistance.progress = 0

                    disactivateInputElements()
                    setFormOfActivity()

                    distanceToGo = (editTextViewDistance.text).toString().toFloat()
                    distanceToGoSet = distanceToGo

                    updateDistanceProgressBar()
                }

                isNewSession = false

                if (running) {
                    running = false
                    buttonStartTracking.text = getString(R.string.continue_tracking)

                } else {

                    if (isFinish) {
                        isFinish = false

                        resetValues()

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
        }

        buttonStopTracking.setOnClickListener {

            running = false
            isFinish = true
            isNewSession = true
            distanceEnd = false

            activateInputElements()

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

    fun disactivateInputElements() {
        radioButtonWalk.isClickable = false
        radioButtonRun.isClickable = false
        radioButtonBicycle.isClickable = false
        editTextViewDistance.isEnabled = false
    }

    fun activateInputElements() {
        radioButtonWalk.isClickable = true
        radioButtonRun.isClickable = true
        radioButtonBicycle.isClickable = true
        editTextViewDistance.isEnabled = true
    }

    fun setFormOfActivity() {

        if (radioButtonWalk.isChecked){
            formOfActivity = FormOfActivity.WALK

        } else if(radioButtonRun.isChecked){
            formOfActivity = FormOfActivity.RUN

        } else if (radioButtonBicycle.isChecked){
            formOfActivity = FormOfActivity.BICYCLE
        }
    }

    fun updateDistanceProgressBar(){
        var progress: Int = (100 - (100 * distanceToGo / distanceToGoSet)).toInt()

        if (progress > 100){
            progress = 100
        }

        progressBarDistance.progress = progress
    }

    fun stopTracking(){
        running = false
        isFinish = true
        isNewSession = true
        distanceEnd = false

        activateInputElements()

        buttonStartTracking.text = getString(R.string.start_tracking)
        buttonStopTracking.isEnabled = false
    }


}