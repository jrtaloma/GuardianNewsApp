package it.sapienza.guardiannewsapp.ui.favorites

import it.sapienza.guardiannewsapp.news.News

interface FavoritesCRUD {
    fun getAll() : Boolean
    fun read(position: Int) : News
    fun getItemCount(): Int
    fun createFavorite(news: News, accountName: String): Boolean
    fun deleteFavorite(news: News, accountName: String): Boolean
}