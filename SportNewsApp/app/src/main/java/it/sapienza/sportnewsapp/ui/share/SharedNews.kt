package it.sapienza.sportnewsapp.ui.share

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SharedNews (
    val sender: String? = null,
    val id: String? = null,
    val webPublicationDate: String? = null,
    val webTitle: String? = null,
    val webUrl: String? = null
)