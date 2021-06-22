package it.sapienza.guardiannewsapp.news

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsWebService {
    @GET("/search")
    suspend fun getNews(@Query("tag") tag: String, @Query("order-by") orderby: String, @Query("api-key") key: String): Response<NewsList>

    @GET("/search")
    suspend fun getQueryResults(@Query("tag") tag: String, @Query("q") q: String, @Query("order-by") orderby: String, @Query("api-key") key: String): Response<NewsList>

    @GET("/search")
    suspend fun getNextNews(@Query("tag") tag: String, @Query("page") page: Int, @Query("order-by") orderby: String, @Query("api-key") key: String): Response<NewsList>

    @GET("/search")
    suspend fun getNextQueryResults(@Query("tag") tag: String, @Query("page") page: Int, @Query("q") q: String, @Query("order-by") orderby: String, @Query("api-key") key: String): Response<NewsList>
}