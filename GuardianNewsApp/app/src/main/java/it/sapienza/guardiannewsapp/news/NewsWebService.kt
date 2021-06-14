package it.sapienza.guardiannewsapp.news

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsWebService {
    @GET("/search")
    suspend fun getNews(@Query("section") section: String, @Query("api-key") key: String): Response<NewsList>

    @GET("/search")
    suspend fun getQueryResults(@Query("section") section: String, @Query("q") q: String, @Query("api-key") key: String): Response<NewsList>

    @GET("/search")
    suspend fun getNextNews(@Query("section") section: String, @Query("page") page: Int, @Query("api-key") key: String): Response<NewsList>

    @GET("/search")
    suspend fun getNextQueryResults(@Query("section") section: String, @Query("page") page: Int, @Query("q") q: String, @Query("api-key") key: String): Response<NewsList>
}