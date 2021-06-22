package it.sapienza.guardiannewsapp.news

interface NewsCRUD {
    fun getAll(tag: String) : Boolean
    fun search(tag: String, query: String): Boolean
    fun getAllNext(tag: String) : Boolean
    fun read(position: Int) : News
    fun getItemCount(): Int
}