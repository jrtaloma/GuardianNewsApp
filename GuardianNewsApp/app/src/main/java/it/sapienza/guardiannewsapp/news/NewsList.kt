package it.sapienza.guardiannewsapp.news

data class NewsList (
    val response: GuardianResponse
)

data class GuardianResponse (
    val status: String,
    val total: Int,
    val currentPage: Int,
    val pages: Int,
    val results: ArrayList<News>
)