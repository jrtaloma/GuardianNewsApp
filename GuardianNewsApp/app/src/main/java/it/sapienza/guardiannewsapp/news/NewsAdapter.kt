package it.sapienza.guardiannewsapp.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

// https://oozou.com/blog/a-better-way-to-handle-click-action-in-a-recyclerview-item-60

class NewsAdapter(private val tag: String, onItemClicked: (News) -> Unit, onItemLongClicked: (News) -> Boolean) : RecyclerView.Adapter<NewsViewHolder>() {

    private var onItemClicked = onItemClicked
    private var onItemLongClicked = onItemLongClicked
    private val repository: NewsRepository = NewsRepository(this)

    fun setOnItemClicked(onItemClicked: (News) -> Unit) {
        this.onItemClicked = onItemClicked
    }

    fun setOnItemLongClicked(onItemLongClicked: (News) -> Boolean) {
        this.onItemLongClicked = onItemLongClicked
    }

    init {
        repository.getAll(tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return NewsViewHolder(
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

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news: News = repository.read(position)
        holder.bind(news)
    }

    override fun getItemCount(): Int {
        return repository.getItemCount()
    }

    fun getAll(): Boolean {
        return repository.getAll(tag)
    }

    fun search(query: String): Boolean {
        return repository.search(tag, query)
    }

    fun getAllNext(): Boolean {
        return repository.getAllNext(tag)
    }

}