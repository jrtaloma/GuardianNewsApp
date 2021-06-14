package it.sapienza.guardiannewsapp.ui.share

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
import kotlinx.android.synthetic.main.fragment_share.*

class ShareFragment : Fragment() {

    private lateinit var googleIdToken: String
    private lateinit var googleEmail: String
    private lateinit var viewModel: SharedViewModel
    private lateinit var adp: ShareAdapter
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
        return inflater.inflate(R.layout.fragment_share, container, false)
    }

    // populate the views now that the layout has been inflated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as HomepageActivity).supportActionBar?.title = viewModel.accountName

        googleIdToken = viewModel.googleIdToken
        googleEmail = viewModel.googleEmail

        if (viewModel.shareAdapter != null) {
            Log.i("info", "Getting ShareAdapter from SharedViewModel")
            adp = viewModel.shareAdapter!!
            adp.setContext((activity as HomepageActivity))
            adp.setOnItemClicked { loadWebPage(it) }
            adp.setOnItemLongClicked { createFavorite(it) }
        } else {
            adp = ShareAdapter((activity as HomepageActivity), googleEmail, { loadWebPage(it) }, { createFavorite(it) })
            viewModel.shareAdapter = adp
        }

        share_list_recycler_view.apply {
            // set a LinearLayoutManager to position items
            layoutManager = LinearLayoutManager(activity)
            // set the custom adapter to populate items
            adapter = adp
            // add borders between items
            addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        }

        // set refresh event listener
        swipeRefreshLayout = view.findViewById(R.id.share_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener { refreshContent() }
    }

    private fun refreshContent() {
        adp.getAll()
        viewModel.shareAdapter = adp
        swipeRefreshLayout.isRefreshing = false
    }

    private fun loadWebPage(news: FirebaseNews) {
        try {
            if (news.id != "") {
                val intent = Intent(activity, WebpageShareActivity::class.java).apply {
                    putExtra(GOOGLE_ID_TOKEN, googleIdToken)
                    putExtra(ACCOUNT_EMAIL, googleEmail)
                    putExtra(FIREBASE_KEY, news.firebaseKey)
                    putExtra(SENDER, news.sender)
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

    private fun createFavorite(news: FirebaseNews) : Boolean {
        if (news.id != "") {
            val n = News(news.id, news.webPublicationDate, news.webTitle, news.webUrl)
            Toast.makeText(activity, "Insertion in favorites", Toast.LENGTH_SHORT).show()
            viewModel.favoritesAdapter?.createFavorite(n, viewModel.accountName)
        }
        return true
    }

}