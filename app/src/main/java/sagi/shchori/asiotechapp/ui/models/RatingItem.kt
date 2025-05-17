package sagi.shchori.asiotechapp.ui.models

import com.google.gson.annotations.SerializedName

data class RatingItem(
    @field:SerializedName("Value")
    val value: String? = null,

    @field:SerializedName("Source")
    val source: String? = null
)