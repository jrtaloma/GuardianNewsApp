package it.sapienza.guardiannewsapp

import androidx.lifecycle.ViewModel
import it.sapienza.guardiannewsapp.news.NewsAdapter
import it.sapienza.guardiannewsapp.ui.favorites.FavoritesAdapter
import it.sapienza.guardiannewsapp.ui.share.ShareAdapter

class SharedViewModel : ViewModel() {
    var accountName: String = ""
    var googleIdToken: String = ""
    var googleEmail: String = ""
    var politicsNewsAdapter: NewsAdapter? = null
    var businessNewsAdapter: NewsAdapter? = null
    var sportNewsAdapter: NewsAdapter? = null
    var favoritesAdapter: FavoritesAdapter? = null
    var shareAdapter: ShareAdapter? = null
}