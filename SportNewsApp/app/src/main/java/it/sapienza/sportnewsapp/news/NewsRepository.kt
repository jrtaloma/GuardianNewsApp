package it.sapienza.sportnewsapp.news

import android.util.Log
import com.google.gson.GsonBuilder
import it.sapienza.sportnewsapp.API_KEY
import it.sapienza.sportnewsapp.URL_GUARDIAN_API
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat

class NewsRepository(adapter: NewsAdapter) : NewsCRUD {

    private val adp: NewsAdapter = adapter
    private val data: MutableMap<Int, News> = mutableMapOf()
    private var total: Int = 0
    private var currentPage: Int = 0
    private var pages: Int = 0
    private var lastQuery: String = ""
    private val proxy: NewsWebService = Retrofit.Builder()
        .baseUrl(URL_GUARDIAN_API)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()
        .create(NewsWebService::class.java)

    override fun getAll(tag: String): Boolean {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = proxy.getNews(tag, "newest", API_KEY)
                if (response.isSuccessful) {
                    data.clear()
                    total = response.body()!!.response.total
                    currentPage = response.body()!!.response.currentPage
                    pages = response.body()!!.response.pages
                    lastQuery = ""
                    val results = response.body()!!.response.results
                    var news: News
                    for (i in 0 until results.size) {
                        val date = results[i].webPublicationDate.subSequence(0, 10).toString()
                        val time = results[i].webPublicationDate.subSequence(11, 19).toString()
                        news = News(results[i].id, date+" "+time, results[i].webTitle, results[i].webUrl)
                        data[i] = news
                        Log.i("info", news.toString())
                    }
                    withContext(Dispatchers.Main) {
                        adp.notifyDataSetChanged()
                    }
                } else {
                    Log.i("info", response.code().toString())
                }
            } catch (e: IOException) {
                val sdf = SimpleDateFormat("HH:mm:ss")
                data.clear()
                data[0] = News("", sdf.format(System.currentTimeMillis()), "No internet connection", "")
                Log.e("error", "No internet connection")
                withContext(Dispatchers.Main) {
                    adp.notifyDataSetChanged()
                }
            }
        }
        return true
    }

    override fun search(tag: String, query: String): Boolean {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = proxy.getQueryResults(tag, query, "newest", API_KEY)
                if (response.isSuccessful) {
                    data.clear()
                    total = response.body()!!.response.total
                    currentPage = response.body()!!.response.currentPage
                    pages = response.body()!!.response.pages
                    lastQuery = query
                    val results = response.body()!!.response.results
                    var news: News
                    for (i in 0 until results.size) {
                        val date = results[i].webPublicationDate.subSequence(0, 10).toString()
                        val time = results[i].webPublicationDate.subSequence(11, 19).toString()
                        news = News(results[i].id, date+" "+time, results[i].webTitle, results[i].webUrl)
                        data[i] = news
                        Log.i("info", news.toString())
                    }
                    withContext(Dispatchers.Main) {
                        adp.notifyDataSetChanged()
                    }
                } else {
                    Log.i("info", response.code().toString())
                }
            } catch (e: IOException) {
                val sdf = SimpleDateFormat("HH:mm:ss")
                data.clear()
                data[0] = News("", sdf.format(System.currentTimeMillis()), "No internet connection", "")
                Log.e("error", "No internet connection")
                withContext(Dispatchers.Main) {
                    adp.notifyDataSetChanged()
                }
            }
        }
        return true
    }

    override fun getAllNext(tag: String): Boolean {
        if (currentPage == total) {
            Log.i("info", "All results retrieved")
            return false
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<NewsList> = if (lastQuery == "")
                    proxy.getNextNews(tag, currentPage+1, "newest", API_KEY)
                else
                    proxy.getNextQueryResults(tag, currentPage+1, lastQuery, "newest", API_KEY)
                if (response.isSuccessful) {
                    total = response.body()!!.response.total
                    currentPage = response.body()!!.response.currentPage
                    pages = response.body()!!.response.pages
                    val results = response.body()!!.response.results
                    var news: News
                    for (i in 0 until results.size) {
                        val date = results[i].webPublicationDate.subSequence(0, 10).toString()
                        val time = results[i].webPublicationDate.subSequence(11, 19).toString()
                        news = News(results[i].id, date+" "+time, results[i].webTitle, results[i].webUrl)
                        data[(currentPage-1)*10+i] = news
                        Log.i("info", news.toString())
                    }
                    withContext(Dispatchers.Main) {
                        adp.notifyDataSetChanged()
                    }
                } else {
                    Log.i("info", response.code().toString())
                }
            } catch (e: IOException) {
                val sdf = SimpleDateFormat("HH:mm:ss")
                data.clear()
                data[0] = News("", sdf.format(System.currentTimeMillis()), "No internet connection", "")
                Log.e("error", "No internet connection")
                withContext(Dispatchers.Main) {
                    adp.notifyDataSetChanged()
                }
            }
        }
        return true
    }

    override fun read(position: Int): News {
        if ((data.isEmpty()) or (data.size < position)) {
            Log.i("info", "Error: can't find the news.. ")
            return News("", "", "Error: can't find the news.. ", "")
        }
        val newsID = data.keys.sorted()[position]
        Log.i("info","Getting newsID "+newsID.toString()+": "+ data[newsID].toString())
        return data[newsID]!!
    }

    override fun getItemCount(): Int {
        return data.size
    }

}