package de.chittalk.messenger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChitTalkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Hier können später Initialisierungen hinzugefügt werden
    }
}