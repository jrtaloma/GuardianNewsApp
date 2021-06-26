package it.sapienza.sportnewsapp.ui.share

interface ShareFirebaseInterface {
    fun getAll() : Boolean
    fun read(position: Int) : FirebaseNews
    fun getItemCount(): Int
}