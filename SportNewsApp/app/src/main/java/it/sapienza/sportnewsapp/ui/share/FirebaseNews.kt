package it.sapienza.sportnewsapp.ui.share

data class FirebaseNews (
    val firebaseKey: String,
    val sender: String,
    val id: String,
    val webPublicationDate: String,
    val webTitle: String,
    val webUrl: String
)