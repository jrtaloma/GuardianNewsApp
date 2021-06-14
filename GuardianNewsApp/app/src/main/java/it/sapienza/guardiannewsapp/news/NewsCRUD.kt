package it.sapienza.guardiannewsapp.news

interface NewsCRUD {
    fun getAll(section: String) : Boolean
    fun search(section: String, query: String): Boolean
    fun getAllNext(section: String) : Boolean
    fun read(position: Int) : News
    fun getItemCount(): Int
}