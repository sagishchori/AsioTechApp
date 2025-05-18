package sagi.shchori.asiotechapp

import android.util.Log

object Logger {

    private const val TAG = "AsioTag"

    fun i(message: String) {
        customeLog(TAG, message, Log.INFO)
    }

    fun d(message: String) {
        customeLog(TAG, message, Log.DEBUG)
    }

    fun e(message: String) {
        customeLog(TAG, message, Log.ERROR)
    }

    fun v(message: String) {
        customeLog(TAG, message, Log.VERBOSE)
    }

    fun customeLog(tag: String, message: String, priority: Int) {
        when(priority) {
            Log.INFO -> Log.i(tag, message)
            Log.DEBUG -> Log.d(tag, message)
            Log.ERROR -> Log.e(tag, message)
            Log.VERBOSE -> Log.v(tag, message)
        }
    }
}