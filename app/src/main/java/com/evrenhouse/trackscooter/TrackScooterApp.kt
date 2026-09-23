package com.evrenhouse.trackscooter

import android.app.Application
import com.evrenhouse.trackscooter.data.ScooterRepository

/**
 * Simple manual DI container: holds the single repository instance so
 * ViewModels can reach the API without a DI framework.
 */
class TrackScooterApp : Application() {

    companion object {
        lateinit var instance: TrackScooterApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    val repository: ScooterRepository by lazy { ScooterRepository() }
}
