package it.sapienza.guardiannewsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import it.sapienza.guardiannewsapp.ui.favorites.FavoritesWebService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.IOException

// https://johncodeos.com/how-to-add-google-login-button-to-your-android-app-using-kotlin/

class MainActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_application_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        google_login_btn.setOnClickListener {
            // sign in with a google account
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // signed in successfully
            val googleId = account?.id ?: ""
            Log.i("Google ID",googleId)

            val googleFirstName = account?.givenName ?: ""
            Log.i("Google First Name", googleFirstName)

            val googleLastName = account?.familyName ?: ""
            Log.i("Google Last Name", googleLastName)

            val googleEmail = account?.email ?: ""
            Log.i("Google Email", googleEmail)

            val googleProfilePicURL = account?.photoUrl.toString()
            Log.i("Google Profile Pic URL", googleProfilePicURL)

            val googleIdToken = account?.idToken ?: ""
            Log.i("Google ID Token", googleIdToken)

            // send token ID to server for verification
            sendTokenId(googleIdToken, googleEmail)

        } catch (e: ApiException) {
            Log.e("error", "Failed code = "+e.statusCode.toString())
            Toast.makeText(this, "Failed code = "+e.statusCode.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendTokenId(googleIdToken: String, googleEmail: String) {
        val proxy = Retrofit.Builder()
            .baseUrl(URL_WEB_API)
            .build()
            .create(FavoritesWebService::class.java)

        val jsonObject = JSONObject()
        jsonObject.put("tokenID", googleIdToken)
        val jsonObjectString = jsonObject.toString()
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = proxy.checkTokenId(requestBody)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // launch Homepage activity
                        val intent = Intent(this@MainActivity, HomepageActivity::class.java).apply {
                            putExtra(GOOGLE_ID_TOKEN, googleIdToken)
                            putExtra(ACCOUNT_EMAIL, googleEmail)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e("error", "This Google account's token ID is not valid")
                            Toast.makeText(this@MainActivity, "This Google account's token ID is not valid", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("error", "Server not reachable")
                    Toast.makeText(this@MainActivity, "Server not reachable", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}