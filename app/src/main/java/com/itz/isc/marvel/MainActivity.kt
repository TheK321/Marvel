package com.itz.isc.marvel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.itz.isc.marvel.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import java.math.BigInteger
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var conn : HttpsURLConnection

    private var total = 100 //Total de datos que se va a pedir a la api
    private var publicKey = "25670ac6c5e9f475fdfeb62f87ea5550" //agregar su llave publica
    private var privateKey = "77acdcf26ea659365802cf2cf7dc31ce97c1fba0" //agregar su llave privada
    private var timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
    private var hash = md5(timeStamp+privateKey+publicKey)

    private var response = ""
    private var nameCharacter = ""
    private var descriptionCharacter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch(Dispatchers.IO){
            val url = URL("https://gateway.marvel.com:443/v1/public/characters?limit=$total&apikey=$publicKey&hash=$hash&ts=$timeStamp")
            conn = url.openConnection() as HttpsURLConnection
            conn.setRequestProperty("Accept", "application/json")
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.doOutput = false

            val responseCode = conn.responseCode
            if(responseCode == HttpsURLConnection.HTTP_OK){
                response = conn.inputStream.bufferedReader(Charset.defaultCharset()).use{ it.readText() }
            }else{
                Log.e("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }
            conn.disconnect()
        }
        binding.button.setOnClickListener{
            val index = (0..total).random()
            getCharacterMarvel(index)
        }
    }

    private fun getCharacterMarvel(index : Int){
        try {
            val jsonObject = JSONTokener(response).nextValue() as JSONObject
            val jData = jsonObject.getJSONObject("data")
            val jResults = jData.getJSONArray("results")
            nameCharacter = jResults.getJSONObject(index).getString("name")
            descriptionCharacter = jResults.getJSONObject(index).getString("description")
            binding.nameCharacter.text = nameCharacter
            binding.descriptionCharacter.text = descriptionCharacter
            val jImage = jResults.getJSONObject(index).getJSONObject("thumbnail")
            println(jImage)
            val imgURL = jImage.getString("path")+"/portrait_uncanny."+jImage.getString("extension")
            println(imgURL)
            loadImage(imgURL)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun loadImage(url : String){
        var bitmap : Bitmap? = null
        lifecycleScope.launch(Dispatchers.IO){
            try {
                val imageStream = URL(url).openStream()
                bitmap = BitmapFactory.decodeStream(imageStream)
                if(bitmap!=null){
                    runOnUiThread { binding.imgvPersonaje.setImageBitmap(bitmap) }
                } else{
                    runOnUiThread {binding.imgvPersonaje.setImageResource(R.drawable.portrait_xlarge)}
                }
            } catch (e : Exception){
                runOnUiThread {binding.imgvPersonaje.setImageResource(R.drawable.portrait_xlarge)}
                e.printStackTrace()
            }
        }
    }

    private fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

}