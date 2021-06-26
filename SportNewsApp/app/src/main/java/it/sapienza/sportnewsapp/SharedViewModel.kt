package it.sapienza.sportnewsapp

import androidx.lifecycle.ViewModel
import it.sapienza.sportnewsapp.news.NewsAdapter
import it.sapienza.sportnewsapp.ui.favorites.FavoritesAdapter
import it.sapienza.sportnewsapp.ui.share.ShareAdapter

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