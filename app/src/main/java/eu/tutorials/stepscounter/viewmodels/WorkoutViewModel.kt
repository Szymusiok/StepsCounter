package eu.tutorials.stepscounter.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import eu.tutorials.stepscounter.model.Workout

class WorkoutViewModel : ViewModel() {
    var summaryWorkout by mutableStateOf<Workout?>(null)
}