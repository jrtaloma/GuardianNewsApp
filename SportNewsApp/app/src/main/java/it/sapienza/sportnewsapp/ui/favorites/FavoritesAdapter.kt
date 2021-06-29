package it.sapienza.sportnewsapp.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.sapienza.sportnewsapp.news.News

// https://oozou.com/blog/a-better-way-to-handle-click-action-in-a-recyclerview-item-60

class FavoritesAdapter(googleIdToken: String, onItemClicked: (News) -> Unit, onItemLongClicked: (News) -> Boolean) : RecyclerView.Adapter<FavoriteViewHolder>() {

    private val tokenID: String = googleIdToken
    private var onItemClicked = onItemClicked
    private var onItemLongClicked = onItemLongClicked
    private val repository: FavoritesRepository = FavoritesRepository(this, tokenID)

    fun setOnItemClicked(onItemClicked: (News) -> Unit) {
        this.onItemClicked = onItemClicked
    }

    fun setOnItemLongClicked(onItemLongClicked: (News) -> Boolean) {
        this.onItemLongClicked = onItemLongClicked
    }

    init {
        repository.getAll()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return FavoriteViewHolder(
            inflater,
            parent,
            {
                //repository.read(it)?.let { item -> onItemClicked(item) }
                onItemClicked(repository.read(it))
            },
            {
                //repository.read(it)?.let {item -> onItemLongClicked(item)}
                onItemLongClicked(repository.read(it))
            }
        )
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val news: News = repository.read(position)
        holder.bind(news)
    }

    override fun getItemCount(): Int {
        return repository.getItemCount()
    }

    fun getAll(): Boolean {
        return repository.getAll()
    }

    fun createFavorite(news: News, accountName: String): Boolean {
        return repository.createFavorite(news, accountName)
    }

    fun deleteFavorite(news: News, accountName: String): Boolean {
        return repository.deleteFavorite(news, accountName)
    }

}