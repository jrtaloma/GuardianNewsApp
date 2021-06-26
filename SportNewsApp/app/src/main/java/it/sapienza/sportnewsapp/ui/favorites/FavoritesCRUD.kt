package it.sapienza.sportnewsapp.ui.favorites

import it.sapienza.sportnewsapp.news.News

interface FavoritesCRUD {
    fun getAll() : Boolean
    fun read(position: Int) : News
    fun getItemCount(): Int
    fun createFavorite(news: News, accountName: String): Boolean
    fun deleteFavorite(news: News, accountName: String): Boolean
}