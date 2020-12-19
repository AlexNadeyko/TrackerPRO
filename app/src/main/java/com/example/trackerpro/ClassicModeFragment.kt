package com.example.trackerpro


//import android.content.Context.getSystemService
//import androidx.core.content.Context.getSystemService
//import androidx.core.content.ContextCompat.getSystemService
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_classic_mode.*
import java.util.*


class ClassicModeFragment : Fragment(), SensorEventListener {

    private var seconds: Int = 0
    private var running: Boolean = false
    private var isPause: Boolean = false
    private var isFinish: Boolean = false
    private var wasRunning: Boolean = false
    private var sManager: SensorManager? = null
//    private var stepSensor: Sensor = sManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sManager = context?.getSystemService(SensorManager::class.java)

        runTimer()
//        val sManager: SensorManager? = context?.getSystemService(SensorManager::class.java)
//        print(sManager)
//        val sManager = getSystemService(context, SensorManager::class.java)
//        val stepSensor: Sensor = sManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
//        print(stepSensor)
//        buttonStartTracking.setOnClickListener { running = true }

//        buttonStartTracking.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(view: View?) {
//                running = true
//            }
//
//        })
//
//        runTimer()
    }

        private fun runTimer() {

        // Get the text view.


        // Creates a new Handler
        val handler = Handler()

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(object : Runnable {
            override fun run() {
                val hours = seconds / 3600
                val minutes = seconds % 3600 / 60
                val secs = seconds % 60

                // Format the seconds into hours, minutes,
                // and seconds.
                val time: String = java.lang.String
                        .format(Locale.getDefault(),
                                "%02d:%02d:%02d", hours,
                                minutes, secs)

                // Set the text view text.
                if (textViewStopwatch != null) {
                    textViewStopwatch.text = time
                }

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000)
            }
        })
    }

//    private fun runStepCounter(){
//        val handler = Handler()
//
//        handler.post(object : Runnable {
//            override fun run() {
//
//                while (running){
//
//                    textViewDistanceCounter =
//                }
//            }
//
//        })
//    }


//    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        buttonStartTracking.setOnClickListener { running = true }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View =  inflater.inflate(R.layout.fragment_classic_mode, container, false)

        val buttonStartTracking = view.findViewById<Button>(R.id.buttonStartTracking)
        val buttonStopTracking = view.findViewById<Button>(R.id.buttonStopTracking)
        val textViewStopWatch = view.findViewById<TextView>(R.id.textViewStopwatch)

        if (running){
            val hours = seconds / 3600
            val minutes = seconds % 3600 / 60
            val secs = seconds % 60

            val time: String = java.lang.String
                    .format(Locale.getDefault(),
                            "%02d:%02d:%02d", hours,
                            minutes, secs)

            textViewStopWatch.text = time

            buttonStartTracking.text = getString(R.string.pause_tracking)
            buttonStopTracking.isEnabled = true
        }





        buttonStartTracking.setOnClickListener {



            if (running){
                running = false
                buttonStartTracking.text = getString(R.string.continue_tracking)

            }else{

                if (isFinish){
                    isFinish = false
                    textViewStopWatch.text = getString(R.string.time_default)
                    seconds = 0
                }

                running = true
                buttonStartTracking.text = getString(R.string.pause_tracking)
                buttonStopTracking.isEnabled = true
            }
        }

        buttonStopTracking.setOnClickListener {

            running = false
            isFinish = true

            buttonStartTracking.text = getString(R.string.start_tracking)
            buttonStopTracking.isEnabled = false

        }

//        runTimer()

        return view
    }

    override fun onResume() {
        super.onResume()
//        sManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onStop() {
        super.onStop()
//        sManager?.unregisterListener(this, stepSensor)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        TODO("Not yet implemented")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

}