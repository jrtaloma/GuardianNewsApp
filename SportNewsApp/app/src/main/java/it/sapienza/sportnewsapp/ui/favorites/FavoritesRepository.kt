package it.sapienza.sportnewsapp.ui.favorites

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import it.sapienza.sportnewsapp.URL_WEB_API
import it.sapienza.sportnewsapp.news.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat

class FavoritesRepository(adapter: FavoritesAdapter, googleIdToken: String) : FavoritesCRUD {

    private val adp: FavoritesAdapter = adapter
    private val tokenID: String = googleIdToken
    private val data: MutableMap<Int, News> = mutableMapOf()

    private val okHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(
            Interceptor { chain ->
                val builder = chain.request().newBuilder()
                builder.header("tokenID", tokenID)
                return@Interceptor chain.proceed(builder.build())
            }
        )
    }.build()

    private val proxy: FavoritesWebService = Retrofit.Builder()
        .baseUrl(URL_WEB_API)
        .client((okHttpClient))
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()
        .create(FavoritesWebService::class.java)

    override fun getAll(): Boolean {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = proxy.getFavorites()
                if (response.isSuccessful) {
                    data.clear()
                    val results = response.body()!!.favorites
                    var news: News
                    for (i in 0 until results.size) {
                        news = News(results[i].id, results[i].webPublicationDate, results[i].webTitle, results[i].webUrl)
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

    override fun createFavorite(news: News, accountName: String): Boolean {
        val jsonObject = JSONObject()
        jsonObject.put("news", JSONObject(Gson().toJson(news)))
        val jsonObjectString = jsonObject.toString()
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = proxy.createFavorite(requestBody)
                if (response.isSuccessful) {
                    var i = 0
                    while (i < data.size) {
                        data[data.size-i] = data[data.size-i-1]!!
                        i++
                    }
                    data[0] = news
                    Log.i("info", "Created favorite: ("+accountName+", "+news.id+")")
                    withContext(Dispatchers.Main) {
                        adp.notifyDataSetChanged()
                    }
                }
                else {
                    Log.e("error", "Favorite not created: ("+accountName+", "+news.id+")")
                }
            } catch (e: IOException) {
                Log.e("error", "No internet connection")
            }
        }

        return true
    }

    override fun deleteFavorite(news: News, accountName: String): Boolean {
        GlobalScope.launch(Dispatchers.IO) {
            try{
                val response = proxy.deleteFavorite(news.id)
                if (response.isSuccessful) {
                    val pos = data.values.indexOf(news)
                    var i = pos
                    while (i < data.size-1) {
                        data[i] = data[i+1]!!
                        i++
                    }
                    data.remove(data.size-1)
                    Log.i("info", "Deleted favorite: ("+accountName+", "+news.id+")")
                    withContext(Dispatchers.Main) {
                        adp.notifyDataSetChanged()
                    }
                }
                else {
                    Log.e("error", "Favorite not deleted: ("+accountName+", "+news.id+")")
                }
            } catch (e: IOException) {
                Log.e("error", "No internet connection")
            }
        }

        return true
    }

}