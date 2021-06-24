package it.sapienza.guardiannewsapp

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.GsonBuilder
import it.sapienza.guardiannewsapp.ui.favorites.FavoritesWebService
import it.sapienza.guardiannewsapp.ui.share.SharedNews
import kotlinx.android.synthetic.main.activity_webpage.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

// https://firebase.google.com/docs/database/android/read-and-write
// https://medium.com/@valokafor/firebase-realtime-database-by-example-with-android-1e597819e24b

class WebpageShareActivity : AppCompatActivity() {

    private var googleIdToken: String? = ""
    private var accountEmail: String? = ""
    private var account: String = ""
    private var firebaseKey: String? = ""
    private var sender: String? = ""
    private var newsID: String? = ""
    private var webPublicationDate: String? = ""
    private var webTitle: String? = ""
    private var webUrl: String? = ""
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var proxy: FavoritesWebService
    private lateinit var database: DatabaseReference
    private lateinit var usersCloudEndPoint: DatabaseReference
    private lateinit var userCloudEndPoint: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webpage)

        googleIdToken = intent.getStringExtra(GOOGLE_ID_TOKEN)
        accountEmail = intent.getStringExtra(ACCOUNT_EMAIL)
        account = accountEmail!!.split("@")[0].replace(".", "")
        firebaseKey = intent.getStringExtra(FIREBASE_KEY)
        sender = intent.getStringExtra(SENDER)
        newsID = intent.getStringExtra(NEWS_ID)
        webPublicationDate = intent.getStringExtra(WEB_PUBLICATION_DATE)
        webTitle = intent.getStringExtra(WEB_TITLE)
        webUrl = intent.getStringExtra(WEB_PAGE_URL)

        okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("tokenID", googleIdToken!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()

        proxy = Retrofit.Builder()
            .baseUrl(URL_WEB_API)
            .client((okHttpClient))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(FavoritesWebService::class.java)

        database = FirebaseDatabase.getInstance("https://guardian-news-app-default-rtdb.europe-west1.firebasedatabase.app/").reference
        usersCloudEndPoint = database.child("users")
        userCloudEndPoint = usersCloudEndPoint.child(account)

        supportActionBar?.title = "By "+ sender!!.split("@")[0]

        web_view.loadUrl(webUrl!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.webpage_share_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.browser2 -> {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(webUrl)
                startActivity(openURL)
                true
            }
            R.id.re_share -> {
                showDialog()
                true
            }
            R.id.delete -> {
                userCloudEndPoint.child(firebaseKey!!).removeValue()
                    .addOnSuccessListener {
                        Log.i("info", "Deleting "+firebaseKey)
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener {
                        Log.e("error", "Firebase database not reachable")
                        Toast.makeText(this, "Firebase database not reachable", Toast.LENGTH_SHORT).show()
                    }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Destination")

        val input = EditText(this)
        input.hint = "Enter user"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Ok") { _, _ ->
            var txt = input.text.toString()
            if ("@" !in txt)
                txt += "@gmail.com"
            val destinationEmail = txt
            checkEmailAndShare(destinationEmail)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun checkEmailAndShare(email: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = proxy.checkEmail(email)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val news = SharedNews(accountEmail, newsID, webPublicationDate, webTitle, webUrl)
                        val path = email.split("@")[0].replace(".", "")
                        val receiverCloudEndpoint = usersCloudEndPoint.child(path)
                        val key = receiverCloudEndpoint.push().key!!
                        receiverCloudEndpoint.child(key).setValue(news)
                            .addOnSuccessListener {
                                Log.i("info",  accountEmail+" sending "+newsID+" to "+email)
                                Toast.makeText(this@WebpageShareActivity, "Sending to "+email, Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Log.e("error", "Firebase database not reachable")
                                Toast.makeText(this@WebpageShareActivity, "Firebase database not reachable", Toast.LENGTH_SHORT).show()

                            }
                    }
                    else {
                        withContext(Dispatchers.Main) {
                            Log.e("error", email+" not found")
                            Toast.makeText(this@WebpageShareActivity, email+" not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("error", "WebServer not reachable")
                    Toast.makeText(this@WebpageShareActivity, "WebServer not reachable", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}