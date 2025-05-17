package sagi.shchori.asiotechapp

import android.util.Log

object Logger {

    private const val TAG = "AsioTag"

    fun i(message: String) {
        costumeLog(TAG, message, Log.INFO)
    }

    fun d(message: String) {
        costumeLog(TAG, message, Log.DEBUG)
    }

    fun e(message: String) {
        costumeLog(TAG, message, Log.ERROR)
    }

    fun v(message: String) {
        costumeLog(TAG, message, Log.VERBOSE)
    }

    fun costumeLog(tag: String,message: String, priority: Int) {
        when(priority) {
            Log.INFO -> Log.i(tag, message)
            Log.DEBUG -> Log.d(tag, message)
            Log.ERROR -> Log.e(tag, message)
            Log.VERBOSE -> Log.v(tag, message)
        }
    }
}