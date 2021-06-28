package it.sapienza.sportnewsapp.ui.share

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.firebase.database.*
import it.sapienza.sportnewsapp.CHANNEL_DESCRIPTION
import it.sapienza.sportnewsapp.CHANNEL_ID
import it.sapienza.sportnewsapp.NOTIFICATION_ID
import java.lang.NullPointerException

// https://developer.android.com/training/notify-user/build-notification
// https://www.geeksforgeeks.org/notifications-in-kotlin/

class ShareRepository(context: Context, adapter: ShareAdapter, googleEmail: String) : ShareFirebaseInterface {

    private var c: Context = context
    private val adp: ShareAdapter = adapter
    private val account: String = googleEmail.split("@")[0].replace(".", "")
    private val data: MutableMap<Int, FirebaseNews> = mutableMapOf()
    private val database: DatabaseReference = FirebaseDatabase.getInstance("https://guardian-news-app-default-rtdb.europe-west1.firebasedatabase.app/").reference
    private val userCloudEndPoint: DatabaseReference = database.child("users").child(account)

    private var size: Int = 0

    fun setContext(c: Context) {
        this.c = c
    }

    override fun getAll(): Boolean {
        val sharedNewsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                data.clear()
                try {
                    val mapOfNews =  dataSnapshot.value as HashMap<String, *>
                    val keys = mapOfNews.keys.sorted().reversed()
                    Log.i("info", keys.toString())
                    for ((i, key) in keys.withIndex()) {
                        val news = mapOfNews[key] as HashMap<String, String>
                        data[i] = FirebaseNews(key, news["sender"].toString(), news["id"].toString(), news["webPublicationDate"].toString(), news["webTitle"].toString(), news["webUrl"].toString())
                    }
                } catch (e: NullPointerException) {
                    Log.e("info", "No news in Firebase Realtime Database")
                }
                finally {
                    adp.notifyDataSetChanged()
                    if (data.size > size) {
                        data[0]?.let { showNotification(it.sender, it.webTitle, it.webUrl) }
                    }
                    size = data.size
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("error", databaseError.message)
            }
        }
        userCloudEndPoint.addValueEventListener(sharedNewsListener)

        return true
    }

    override fun read(position: Int): FirebaseNews {
        if ((data.isEmpty()) or (data.size < position)) {
            Log.i("info", "Error: can't find the news.. ")
            return FirebaseNews("", "","", "", "Error: can't find the news.. ", "")
        }
        val newsID = data.keys.sorted()[position]
        Log.i("info","Getting newsID "+newsID.toString()+": "+ data[newsID].toString())
        return data[newsID]!!
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun showNotification(email: String, title: String, url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.parse(url)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(c, 0, intent, 0)
        val notificationManager: NotificationManager = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder: Notification.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_DESCRIPTION, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification.Builder(c, CHANNEL_ID)
                .setSmallIcon(it.sapienza.sportnewsapp.R.drawable.share_colored)
                .setShowWhen(true)
                .setContentTitle(email)
                .setStyle(Notification.BigTextStyle().bigText(title))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            builder = Notification.Builder(c)
                .setSmallIcon(it.sapienza.sportnewsapp.R.drawable.share_colored)
                .setShowWhen(true)
                .setContentTitle(email)
                .setStyle(Notification.BigTextStyle().bigText(title))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

}