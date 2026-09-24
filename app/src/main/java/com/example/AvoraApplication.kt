package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

class AvoraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebase(this)
    }

    companion object {
        private const val TAG = "AvoraApplication"

        fun initFirebase(context: Context) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
            } catch (e: Exception) {
                Log.i(TAG, "Firebase initialization status: ${e.message ?: "Running in local offline/guest mode."}")
            }
        }
    }
}
