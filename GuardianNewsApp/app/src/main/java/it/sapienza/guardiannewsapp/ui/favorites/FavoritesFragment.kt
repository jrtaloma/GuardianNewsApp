package it.sapienza.guardiannewsapp.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import it.sapienza.guardiannewsapp.*
import it.sapienza.guardiannewsapp.news.News
import kotlinx.android.synthetic.main.fragment_favorites.*

// https://guides.codepath.com/android/using-the-recyclerview
// https://www.geeksforgeeks.org/how-to-implement-swipe-down-to-refresh-in-android-using-android-studio/

// https://medium.com/androiddevelopers/viewmodels-a-simple-example-ed5ac416317e
// https://betterprogramming.pub/everything-to-understand-about-viewmodel-400e8e637a58

class FavoritesFragment : Fragment() {

    private lateinit var googleIdToken: String
    private lateinit var googleEmail: String
    private lateinit var viewModel: SharedViewModel
    private lateinit var adp: FavoritesAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        } ?: throw Exception("Activity is null")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    // populate the views now that the layout has been inflated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as HomepageActivity).supportActionBar?.title = viewModel.accountName

        googleIdToken = viewModel.googleIdToken
        googleEmail = viewModel.googleEmail

        if (viewModel.favoritesAdapter != null) {
            Log.i("info", "Getting FavoritesAdapter from SharedViewModel")
            adp = viewModel.favoritesAdapter!!
            adp.setOnItemClicked { loadWebPage(it) }
            adp.setOnItemLongClicked { deleteFavorite(it) }
        } else {
            adp = FavoritesAdapter(googleIdToken, { loadWebPage(it) }, { deleteFavorite(it) } )
            viewModel.favoritesAdapter = adp
        }

        favorites_list_recycler_view.apply {
            // set a LinearLayoutManager to position items
            layoutManager = LinearLayoutManager(activity)
            // set the custom adapter to populate items
            adapter = adp
            // add borders between items
            addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        }

        // set refresh event listener
        swipeRefreshLayout = view.findViewById(R.id.favorites_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener { refreshContent() }
    }

    private fun refreshContent() {
        adp.getAll()
        viewModel.favoritesAdapter = adp
        swipeRefreshLayout.isRefreshing = false
    }

    private fun loadWebPage(news: News) {
        try {
            if (news.id != "") {
                val intent = Intent(activity, WebpageActivity::class.java).apply {
                    putExtra(GOOGLE_ID_TOKEN, googleIdToken)
                    putExtra(ACCOUNT_EMAIL, googleEmail)
                    putExtra(NEWS_ID, news.id)
                    putExtra(WEB_PUBLICATION_DATE, news.webPublicationDate)
                    putExtra(WEB_TITLE, news.webTitle)
                    putExtra(WEB_PAGE_URL, news.webUrl)
                }
                startActivity(intent)
            }
        } catch (e: NullPointerException) {
            Log.e("error", e.toString())
        }
    }

    private fun deleteFavorite(news: News) : Boolean {
        if (news.id != "") {
            Toast.makeText(activity, "Deletion from favorites", Toast.LENGTH_SHORT).show()
            return adp.deleteFavorite(news, viewModel.accountName)
        }
        return true
    }

}