package com.evrenhouse.trackscooter.ui.common

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.evrenhouse.trackscooter.TrackScooterApp
import com.evrenhouse.trackscooter.data.ScooterRepository

/**
 * Factory that resolves the repository from the Application container and
 * passes it (plus a SavedStateHandle when requested) to ViewModels.
 * Supported constructors: (ScooterRepository, SavedStateHandle), (ScooterRepository), ().
 */
class AppViewModelFactory(
    private val repository: ScooterRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val handle = extras.createSavedStateHandle()

        val withHandle = runCatching {
            modelClass.getConstructor(ScooterRepository::class.java, SavedStateHandle::class.java)
        }.getOrNull()
        if (withHandle != null) return withHandle.newInstance(repository, handle)

        val withRepo = runCatching {
            modelClass.getConstructor(ScooterRepository::class.java)
        }.getOrNull()
        if (withRepo != null) return withRepo.newInstance(repository)

        return modelClass.getConstructor().newInstance()
    }
}

/** Resolve the repository from the Application container (usable in default params). */
@androidx.compose.runtime.Composable
fun repository(): ScooterRepository {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext
    return (app as TrackScooterApp).repository
}
