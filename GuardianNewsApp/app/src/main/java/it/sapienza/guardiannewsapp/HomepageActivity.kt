package it.sapienza.guardiannewsapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.GsonBuilder
import it.sapienza.guardiannewsapp.databinding.ActivityHomepageBinding
import it.sapienza.guardiannewsapp.ui.favorites.FavoritesWebService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


// https://android--code.blogspot.com/2018/04/android-kotlin-alertdialog-yes-no.html
// https://proandroiddev.com/headers-in-retrofit-a8d71ede2f3e

class HomepageActivity : AppCompatActivity() {

    private lateinit var googleIdToken: String
    private lateinit var googleEmail: String
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var viewModel: SharedViewModel
    private lateinit var binding: ActivityHomepageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleIdToken = intent.getStringExtra(GOOGLE_ID_TOKEN)!!
        googleEmail = intent.getStringExtra(ACCOUNT_EMAIL)!!

        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        viewModel.googleIdToken = googleIdToken
        viewModel.googleEmail = googleEmail

        val navView: BottomNavigationView = binding.navView
        navView.itemIconTintList = null

        val navController = findNavController(R.id.nav_host_fragment_activity_homepage)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_politics, R.id.navigation_business, R.id.navigation_sport, R.id.navigation_favorites, R.id.navigation_share
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // GoogleSignInOptions
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_application_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        viewModel.accountName = account?.givenName!!
        Log.i("info", "Signed-in account: "+ account.givenName)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.signout_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out -> {
                signOut()
                true
            }
            R.id.revoke_access -> {
                revokeAccess()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                val intent = Intent(this, MainActivity::class.java).apply {}
                startActivity(intent)
                finish()
            }
    }

    private fun revokeAccess() {
        deleteAccountDialog()
    }

    private fun deleteAccountDialog() {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete account")
        builder.setMessage("Are you sure? All contents will be deleted")

        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    mGoogleSignInClient.revokeAccess()
                        .addOnCompleteListener(this) {

                            Toast.makeText(this, "Deleting account", Toast.LENGTH_SHORT).show()

                            val okHttpClient = OkHttpClient.Builder().apply {
                                addInterceptor(
                                    Interceptor { chain ->
                                        val builder = chain.request().newBuilder()
                                        builder.header("tokenID", googleIdToken)
                                        return@Interceptor chain.proceed(builder.build())
                                    }
                                )
                            }.build()

                            val proxy: FavoritesWebService = Retrofit.Builder()
                                .baseUrl(URL_WEB_API)
                                .client((okHttpClient))
                                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                                .build()
                                .create(FavoritesWebService::class.java)

                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val response = proxy.deleteAccount()
                                    if (response.isSuccessful) {
                                        val account = googleEmail.split("@")[0].replace(".", "")
                                        val database: DatabaseReference = FirebaseDatabase.getInstance("https://guardian-news-app-default-rtdb.europe-west1.firebasedatabase.app/").reference
                                        val usersCloudEndPoint: DatabaseReference = database.child("users")
                                        val userCloudEndPoint: DatabaseReference = usersCloudEndPoint.child(account)
                                        userCloudEndPoint.removeValue()
                                            .addOnSuccessListener {
                                                Log.i("info", "Account: "+viewModel.accountName+" deleted")
                                            }
                                            .addOnFailureListener {
                                                Log.e("error", "Account: "+viewModel.accountName+" NOT deleted on Firebase Realtime Database")
                                            }
                                        withContext(Dispatchers.Main) {
                                            val intent = Intent(this@HomepageActivity, MainActivity::class.java).apply {}
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                    else {
                                        Log.e("error", "Account: "+viewModel.accountName+" NOT deleted")
                                    }
                                } catch (e: IOException) {
                                    Log.e("error", "No internet connection")
                                }
                            }

                        }
                }
                DialogInterface.BUTTON_NEGATIVE -> {}
                DialogInterface.BUTTON_NEUTRAL -> {}
            }
        }

        builder.setPositiveButton("YES", dialogClickListener)
        builder.setNegativeButton("NO", dialogClickListener)
        builder.setNeutralButton("CANCEL", dialogClickListener)

        dialog = builder.create()
        dialog.show()
    }

}