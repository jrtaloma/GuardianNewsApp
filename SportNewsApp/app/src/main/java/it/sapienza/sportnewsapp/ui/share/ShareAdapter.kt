package it.sapienza.sportnewsapp.ui.share

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

// https://oozou.com/blog/a-better-way-to-handle-click-action-in-a-recyclerview-item-60

class ShareAdapter(context: Context, googleEmail: String, onItemClicked: (FirebaseNews) -> Unit, onItemLongClicked: (FirebaseNews) -> Boolean) : RecyclerView.Adapter<FirebaseNewsViewHolder>() {

    private val email: String = googleEmail
    private var onItemClicked = onItemClicked
    private var onItemLongClicked = onItemLongClicked
    private val repository: ShareRepository = ShareRepository(context,this, email)

    fun setContext(c: Context) {
        repository.setContext(c)
    }

    fun setOnItemClicked(onItemClicked: (FirebaseNews) -> Unit) {
        this.onItemClicked = onItemClicked
    }

    fun setOnItemLongClicked(onItemLongClicked: (FirebaseNews) -> Boolean) {
        this.onItemLongClicked = onItemLongClicked
    }

    init {
        repository.getAll()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirebaseNewsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return FirebaseNewsViewHolder(
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

    override fun onBindViewHolder(holder: FirebaseNewsViewHolder, position: Int) {
        val news: FirebaseNews = repository.read(position)
        holder.bind(news)
    }

    override fun getItemCount(): Int {
        return repository.getItemCount()
    }

    fun getAll(): Boolean {
        return repository.getAll()
    }

}