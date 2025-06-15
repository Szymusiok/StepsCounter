package eu.tutorials.stepscounter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import eu.tutorials.stepscounter.viewmodels.StepsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrackingService : Service() {

    companion object {
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIF_ID = 1

        const val ACTION_START = "eu.tutorials.stepscounter.action.START"
        const val ACTION_PAUSE = "eu.tutorials.stepscounter.action.PAUSE"
        const val ACTION_RESUME = "eu.tutorials.stepscounter.action.RESUME"
        const val ACTION_STOP = "eu.tutorials.stepscounter.action.STOP"

        val isTracking = MutableStateFlow(false)
        val isPaused = MutableStateFlow(false)
        val pathPoints = MutableStateFlow<List<LatLng>>(emptyList())
        val totalDistance = MutableStateFlow(0.0)
        val calories = MutableStateFlow(0.0)
        val elapsedTime = MutableStateFlow(0L)
        val speed = MutableStateFlow(0.0)
        val pace = MutableStateFlow(0.0)
        val steps = MutableStateFlow(0)

        fun start(ctx: Context) {
            ContextCompat.startForegroundService(
                ctx,
                Intent(ctx, TrackingService::class.java).setAction(ACTION_START)
            )
        }

        fun pause(ctx: Context) {
            ContextCompat.startForegroundService(
                ctx,
                Intent(ctx, TrackingService::class.java).setAction(ACTION_PAUSE)
            )
        }

        fun resume(ctx: Context) {
            ContextCompat.startForegroundService(
                ctx,
                Intent(ctx, TrackingService::class.java).setAction(ACTION_RESUME)
            )
        }

        fun stop(ctx: Context) {
            ContextCompat.startForegroundService(
                ctx,
                Intent(ctx, TrackingService::class.java).setAction(ACTION_STOP)
            )
        }
    }

    private lateinit var tracker: LocationTracker
    private lateinit var stepsViewModel: StepsViewModel
    private val prefs by lazy {
        getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var timerJob: Job? = null
    private var locationJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        tracker = LocationTracker(this)
        stepsViewModel = StepsViewModel(application)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        locationJob?.cancel()
        scope.cancel()
    }

    private fun startTracking() {
        if (isTracking.value) return

        scope.launch {
            val startLoc = tracker.getCurrentLocation() ?: return@launch
            pathPoints.value = listOf(startLoc)
            totalDistance.value = 0.0
            calories.value = 0.0
            elapsedTime.value = 0L
            speed.value = 0.0
            pace.value = 0.0
            isPaused.value = false
            isTracking.value = true

            startForeground(NOTIF_ID, buildNotification())
            stepsViewModel.startTracking()
            startTimer()
            startLocationUpdates()
        }
    }

    private fun pauseTracking() {
        if (!isTracking.value || isPaused.value) return
        isPaused.value = true
        timerJob?.cancel()
        locationJob?.cancel()
        stepsViewModel.stopTracking()
        updateNotification()
    }

    private fun resumeTracking() {
        if (!isTracking.value || !isPaused.value) return
        isPaused.value = false
        stepsViewModel.resumeTracking()
        startTimer()
        startLocationUpdates()
    }

    private fun stopTracking() {
        if (!isTracking.value) return
        isTracking.value = false
        isPaused.value = false
        timerJob?.cancel()
        locationJob?.cancel()
        stepsViewModel.stopTracking()
        stopForeground(true)
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isTracking.value && !isPaused.value) {
                delay(1000L)
                elapsedTime.update { it + 1000L }
                steps.value = stepsViewModel.steps.value
                updateMetrics()
                updateNotification()
            }
        }
    }

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = scope.launch {
            tracker.locationUpdates().collect { newPoint ->
                if (!isTracking.value || isPaused.value) return@collect

                val prev = pathPoints.value.lastOrNull()
                pathPoints.update { it + newPoint }

                prev?.let {
                    val delta = SphericalUtil.computeDistanceBetween(it, newPoint)
                    totalDistance.update { d -> d + delta }
                }
                calories.value = (totalDistance.value / 1000.0) * 60.0
                updateMetrics()
            }
        }
    }

    private fun updateMetrics() {
        val hours = elapsedTime.value / 3_600_000.0
        speed.value = if (hours > 0) (totalDistance.value / 1000.0) / hours else 0.0

        val distKm = totalDistance.value / 1000.0
        pace.value = if (distKm > 0) (elapsedTime.value / 60_000.0) / distKm else 0.0
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Workout in progress")
            .setContentText(notificationText())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        if (!prefs.getBoolean("notifications_enabled", true)) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, buildNotification())
    }

    private fun notificationText(): String {
        val dist = String.format("%.2f km", totalDistance.value / 1000.0)
        val time = formatTime(elapsedTime.value)
        return "$dist • $time"
    }

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val hrs = totalSec / 3600
        val min = (totalSec % 3600) / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d:%02d", hrs, min, sec)
    }
}