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
import kotlinx.android.synthetic.main.fragment_classic_mode.*
import java.util.*


class TimeModeFragment : Fragment() {

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

    private var secondsToGoSet = 0
    private var secondsToGo = 0
    private lateinit var formOfActivity: FormOfActivity

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var editTextHours: EditText
    private lateinit var editTextMinutes: EditText
    private lateinit var editTextSeconds: EditText
    private lateinit var textViewSpeed: TextView
    private lateinit var textViewAverageSpeed: TextView
    private lateinit var textViewCalories: TextView
    private lateinit var textViewDistance: TextView
    private lateinit var radioButtonWalk: RadioButton
    private lateinit var radioButtonRun: RadioButton
    private lateinit var radioButtonBicycle: RadioButton
    private lateinit var progressBarTime: ProgressBar
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

                            val distanceString: String = java.lang.String
                                    .format(
                                            Locale.getDefault(),
                                            "%.0f m", distance
                                    )

                            textViewDistance.text = distanceString
                            textViewSpeed.text = speedString

                            locationWasChanged = true

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
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), TimeModeFragment.LOCATION_PERMISSION_REQUEST_CODE)

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

        } else if (formOfActivity == FormOfActivity.BICYCLE) {
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

                if (secondsToGo > 0 && running){

                    val hours = secondsToGo / 3600
                    val minutes = secondsToGo % 3600 / 60
                    val secs = secondsToGo % 60

                    if (editTextHours != null) {
                        editTextHours.setText(hours.toString())
                    }

                    if (editTextMinutes != null) {
                        editTextMinutes.setText(minutes.toString())
                    }

                    if (editTextSeconds != null) {
                        editTextSeconds.setText(secs.toString())
                    }

                    if (running) {
                        seconds++
                        secondsToGo--

                        displayValuesOfTracking()
                        updateDistanceProgressBar()
                    }

                } else if (secondsToGo <= 0 && running) {
                    stopTracking()
                    displayValuesOfTracking()

                    val hours = secondsToGo / 3600
                    val minutes = secondsToGo % 3600 / 60
                    val secs = secondsToGo % 60

                    if (editTextHours != null) {
                        editTextHours.setText(hours.toString())
                    }

                    if (editTextMinutes != null) {
                        editTextMinutes.setText(minutes.toString())
                    }

                    if (editTextSeconds != null) {
                        editTextSeconds.setText(secs.toString())
                    }

                    updateDistanceProgressBar()
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View =  inflater.inflate(R.layout.fragment_time_mode, container, false)

        buttonStartTracking = view.findViewById<Button>(R.id.buttonStartTrackingTimeMode)
        buttonStopTracking = view.findViewById<Button>(R.id.buttonStopTrackingTimeMode)

        progressBarTime = view.findViewById(R.id.progressBarTimeMode)
        editTextHours = view.findViewById(R.id.editTextNumberHours)
        editTextMinutes = view.findViewById(R.id.editTextNumberMinutes)
        editTextSeconds = view.findViewById(R.id.editTextNumberSeconds)
        textViewSpeed = view.findViewById(R.id.textViewSpeedCounterTimeMode)
        textViewAverageSpeed = view.findViewById(R.id.textViewAverageSpeedCounterTimeMode)
        textViewCalories = view.findViewById(R.id.textViewCaloriesCounterTimeMode)
        radioButtonWalk = view.findViewById(R.id.radioButtonWalkTimeMode)
        radioButtonRun = view.findViewById(R.id.radioButtonRunTimeMode)
        radioButtonBicycle = view.findViewById(R.id.radioButtonBicycleTimeMode)
        textViewDistance = view.findViewById(R.id.textViewDistanceCounterTimeMode)

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

            editTextHours.setText(hours)
            editTextMinutes.setText(minutes)
            editTextSeconds.setText(secs)

            buttonStartTracking.text = getString(R.string.pause_tracking)
            buttonStopTracking.isEnabled = true
        }

        buttonStartTracking.setOnClickListener {

            if ((editTextHours.text).toString() == "") {
                editTextHours.requestFocus()

            } else if ((editTextMinutes.text).toString() == "") {
                editTextMinutes.requestFocus()

            } else if ((editTextSeconds.text).toString() == "") {
                editTextSeconds.requestFocus()

            } else {

                if (isNewSession) {

                    progressBarTime.progress = 100

                    disactivateInputElements()
                    setFormOfActivity()

                    secondsToGoSet = (editTextHours.text).toString().toInt() * 60 * 60 + (editTextMinutes.text).toString().toInt() * 60 +
                            (editTextSeconds.text).toString().toInt()

                    Log.e("SecondsToGoSet", secondsToGoSet.toString())

                    secondsToGo = secondsToGoSet

                    if (secondsToGo != 0) {
                        updateDistanceProgressBar()
                    }
                }

                isNewSession = false

                if (running) {
                    running = false
                    buttonStartTracking.text = getString(R.string.continue_tracking)

                } else {

                    if (isFinish) {
                        isFinish = false

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

                    if (secondsToGo == 0) {
                        stopTracking()
                    } else {
                        running = true
                        buttonStartTracking.text = getString(R.string.pause_tracking)
                        buttonStopTracking.isEnabled = true
                    }
                }
            }
        }

        buttonStopTracking.setOnClickListener {

            running = false
            isFinish = true
            isNewSession = true

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
        editTextHours.isEnabled = false
        editTextMinutes.isEnabled = false
        editTextSeconds.isEnabled = false
    }

    fun activateInputElements() {
        radioButtonWalk.isClickable = true
        radioButtonRun.isClickable = true
        radioButtonBicycle.isClickable = true
        editTextHours.isEnabled = true
        editTextMinutes.isEnabled = true
        editTextSeconds.isEnabled = true
    }

    fun setFormOfActivity() {

        if (radioButtonWalk.isChecked) {
            formOfActivity = FormOfActivity.WALK

        }else if (radioButtonRun.isChecked) {
            formOfActivity = FormOfActivity.RUN

        }else if (radioButtonBicycle.isChecked) {
            formOfActivity = FormOfActivity.BICYCLE
        }
    }

    fun updateDistanceProgressBar(){
        var progress: Int = (100 * secondsToGo / secondsToGoSet).toInt()

        Log.e("Progress", progress.toString())

        progressBarTime.progress = progress
    }

    fun stopTracking() {
        running = false
        isFinish = true
        isNewSession = true

        activateInputElements()

        buttonStartTracking.text = getString(R.string.start_tracking)
        buttonStopTracking.isEnabled = false
    }

}