package it.sapienza.guardiannewsapp

import androidx.lifecycle.ViewModel
import it.sapienza.guardiannewsapp.news.NewsAdapter
import it.sapienza.guardiannewsapp.ui.favorites.FavoritesAdapter
import it.sapienza.guardiannewsapp.ui.share.ShareAdapter

class SharedViewModel : ViewModel() {
    var accountName: String = ""
    var googleIdToken: String = ""
    var googleEmail: String = ""
    var formulaOneNewsAdapter: NewsAdapter? = null
    var tennisNewsAdapter: NewsAdapter? = null
    var cyclingNewsAdapter: NewsAdapter? = null
    var favoritesAdapter: FavoritesAdapter? = null
    var shareAdapter: ShareAdapter? = null
}