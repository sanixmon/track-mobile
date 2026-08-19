package com.evrenhouse.trackscooter

import android.app.Application
import com.evrenhouse.trackscooter.data.ScooterRepository

/**
 * Simple manual DI container: holds the single repository instance so
 * ViewModels can reach the API without a DI framework.
 */
class TrackScooterApp : Application() {

    val repository: ScooterRepository by lazy { ScooterRepository() }
}
