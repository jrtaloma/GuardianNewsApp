package it.sapienza.sportnewsapp.ui.favorites

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface FavoritesWebService {
    @POST("/tokensignin")
    suspend fun checkTokenId(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("/checkemail")
    suspend fun checkEmail(@Query("email") email: String): Response<ResponseBody>

    @GET("/favorites")
    suspend fun getFavorites(): Response<FavoritesResponse>

    @POST("/createfavorite")
    suspend fun createFavorite(@Body requestBody: RequestBody): Response<ResponseBody>

    @DELETE("/deletefavorite")
    suspend fun deleteFavorite(@Query("newsid") newsID: String): Response<ResponseBody>

    @DELETE("/deleteaccount")
    suspend fun deleteAccount(): Response<ResponseBody>
}