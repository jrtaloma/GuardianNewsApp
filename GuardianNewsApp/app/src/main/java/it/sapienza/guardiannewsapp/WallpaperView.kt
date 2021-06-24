package it.sapienza.guardiannewsapp

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset


// https://developer.android.com/codelabs/advanced-android-kotlin-training-custom-views#3
// https://unsplash.com/collections/1005040/sport

class WallpaperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var bitMap: Bitmap? = null

    private val displayMetrics = DisplayMetrics()
    private var displayWidth = 0
    private var displayHeight = 0

    private var resizedBitMapWidth = 0
    private var resizedBitMapHeight = 0

    private var name: String? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            (context as MainActivity).display?.getRealMetrics(displayMetrics)
        } else {
            val windowManager = (context as MainActivity).getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        displayWidth = displayMetrics.widthPixels
        displayHeight = displayMetrics.heightPixels

        val thread = Thread {
            try {
                // Get random photo from Unsplash
                val json = getRandomPhotoFromUnsplash()

                // Parsing JSONObject
                val url = json.getJSONObject("urls").get("regular").toString()
                name = json.getJSONObject("user").get("name").toString()
                Log.i("info", name+": "+url)

                // Load retrieved image
                val inputStream = URL(url).content as InputStream
                bitMap = BitmapFactory.decodeStream(inputStream)
                bitMap = getResizedBitmap(bitMap!!, displayWidth, displayHeight)
                invalidate()
            } catch (e: Exception) {
                Log.e("error", e.stackTraceToString())
            }
        }
        thread.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        bitMap?.let { canvas?.drawBitmap(it, (displayWidth-resizedBitMapWidth)/2f, 0f, null) }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                name?.let { Toast.makeText((context as MainActivity), "By "+name+" on Unsplash", Toast.LENGTH_SHORT).show() }
            }
        }
        return true
    }

    private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height

        val matrix = Matrix()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            matrix.postScale(scaleHeight, scaleHeight)
            resizedBitMapWidth = (width*scaleHeight).toInt()
            resizedBitMapHeight = newHeight
        }
        else {
            matrix.postScale(scaleWidth, scaleWidth)
            resizedBitMapWidth = newWidth
            resizedBitMapHeight = (height*scaleWidth).toInt()
        }

        // recreate the new Bitmap
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }

    private fun getRandomPhotoFromUnsplash(): JSONObject {
        val sb = StringBuilder()
        val inputStream: InputStream
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            inputStream = URL("https://api.unsplash.com/photos/random?collections=1005040&orientation=portrait&client_id="+UNSPLASH_ACCESS_KEY).content as InputStream
        else
            inputStream = URL("https://api.unsplash.com/photos/random?collections=1005040&orientation=landscape&client_id="+UNSPLASH_ACCESS_KEY).content as InputStream
        val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            sb.append(line)
            line = bufferedReader.readLine()
        }
        return JSONObject(sb.toString())
    }

}