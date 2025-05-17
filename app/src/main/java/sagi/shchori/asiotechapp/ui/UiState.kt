package sagi.shchori.asiotechapp.ui

/**
 * A class to represent the state of the UI and handle it accordingly
 */
sealed class UiState<out T> {

    /**
     * Nothing is happening and wait for user input
     */
    data object IDLE : UiState<Nothing>()

    /**
     * A loading process is starting
     */
    data object LOADING : UiState<Nothing>()

    /**
     * An error occurred during some process on the UI, this can be used to send the error
     */
    data class ERROR(val error: String?) : UiState<String>()
}