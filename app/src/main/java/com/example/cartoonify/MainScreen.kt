package com.example.cartoonify

import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainScreen : AppCompatActivity() {

    var modelNumberSelected = 99

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val textField = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.menu)
//        val items = listOf("Option 1", "Option 2", "Option 3", "Option 4")
//        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
//        (textField.editText as? AutoCompleteTextView)?.setAdapter(adapter)
//        val items = arrayOf("Item 1", "Item 2", "Item 3", "Item 4")
//        (textField.editText as? MaterialAutoCompleteTextView)?.setSimpleItems(items)
        val items = listOf("Hosoda", "Hayao", "Shinkai", "Paprika")
        val adapter = ArrayAdapter(applicationContext, R.layout.list_item, items)
        (textField.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        val selectedModel = findViewById<AutoCompleteTextView>(R.id.model)
        println(selectedModel.text)
        selectedModel.onItemClickListener = OnItemClickListener { parent, arg1, pos, id ->
            val selectedModel = parent.getItemAtPosition(pos).toString()
            val uploadedImage = findViewById<ImageView>(R.id.uploadedImage)

            if(selectedModel == "Hosoda"){
                modelNumberSelected = 0
            }
            if(selectedModel == "Hayao"){
                modelNumberSelected = 1
            }
            if(selectedModel == "Shinkai"){
                modelNumberSelected = 2
            }
            if(selectedModel == "Paprika"){
                modelNumberSelected = 3
            }

            if(uploadedImage.drawable != null){
                callAPI(modelNumberSelected)
            }

        }
    }

    fun uploadButtonOnClick(view: View){

        var permissionsString = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            // Permission is not granted
            ActivityCompat.requestPermissions(this@MainScreen, permissionsString, 1005)

        }
        else{
            uploadImagefromStorage()
        }

    }
    fun downloadButtonOnClick(view: View){

        var permissionsString = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            // Permission is not granted
            ActivityCompat.requestPermissions(this@MainScreen, permissionsString, 1005)

        }
        else{
            val apiedImage = findViewById<ImageView>(R.id.apiedImage)

            if(apiedImage.drawable == null) {
                Toast.makeText(this@MainScreen, "No Image", Toast.LENGTH_SHORT).show()
            }
            else {
                saveToDownloadsFolder(drawableToBitmap(apiedImage.drawable))
                Toast.makeText(this@MainScreen, "Image saved in Downloads folder!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    private fun saveToDownloadsFolder(bitmapImage: Bitmap?): Uri? {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val sdf = SimpleDateFormat("ddMyyyyhhmmss")
        val currentDate = sdf.format(Date())
        val time = currentDate as String
        println(time)
        val mypath = File(directory.absolutePath, "cartoonified_image_$time.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage?.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val uri = FileProvider.getUriForFile(applicationContext, "com.example.cartoonify"+".provider",mypath)
        println(uri)
        return uri
    }

    private fun returnFile(bitmapImage: Bitmap?): File? {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val sdf = SimpleDateFormat("ddMyyyyhhmmss")
        val currentDate = sdf.format(Date())
        val time = currentDate as String
        println(time)
        val mypath = File(directory.absolutePath, "cartoonified_image$time.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage?.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val uri = FileProvider.getUriForFile(applicationContext, "com.example.cartoonify"+".provider",mypath)
        println(uri)
        return mypath
    }

    fun shareButtonOnClick(view: View) {
//        Toast.makeText(this@MainScreen, "Share Button", Toast.LENGTH_SHORT).show()
        val apiedImage = findViewById<ImageView>(R.id.apiedImage)
        if(apiedImage.drawable == null){
            Toast.makeText(this@MainScreen, "No Image", Toast.LENGTH_SHORT).show()
        }
        else {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/jpeg"
            val imageUri: Uri? = saveToDownloadsFolder(drawableToBitmap(apiedImage.drawable))
            val buff: Uri? = null
            share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            share.putExtra(Intent.EXTRA_STREAM, imageUri)
            share.putExtra(Intent.EXTRA_SUBJECT, "New App")
            share.putExtra(Intent.EXTRA_TEXT, "Check this out guys! I got this image from the Cartoonify App!")
            //startActivity(Intent.createChooser(share, "Sharing Cartoonified Image"))

            val chooser = Intent.createChooser(share, "Share File")

            val resInfoList = this.packageManager.queryIntentActivities(
                chooser,
                PackageManager.MATCH_DEFAULT_ONLY
            )

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(
                    packageName,
                    imageUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            startActivity(chooser)

        }
    }

    private fun saveImageInAppData(image: Bitmap?): Uri? {
        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", MODE_PRIVATE)
        //val myDirectory = File(applicationContext.cacheDir, "imageDir")
        //val myDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS)
        val mypath = File(directory, "shared_image.jpg")
        var uri: Uri? = null
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            image?.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        //uri = FileProvider.getUriForFile(applicationContext, "com.example.cartoonify"+".provider",mypath)
        return uri
    }

    private fun uploadImagefromStorage(){
        imageChooser()

    }

    private fun imageChooser() {

        // create an instance of the
        // intent of the type image
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 200)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 200) {
                // Get the url of the image from data
                val uploadedImage = findViewById<ImageView>(R.id.uploadedImage)
                val uploadButton = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.uploadButton)
                val selectedImageUri: Uri? = data?.data
                val afd = selectedImageUri?.let { contentResolver.openAssetFileDescriptor(it, "r") }
                val size = afd!!.length
                afd.close()
                println(size)
                if (size <= 2203804 && null != selectedImageUri) {
                    // update the preview image in the layout
                    uploadedImage.setImageURI(selectedImageUri)
                    uploadedImage.visibility = View.VISIBLE
//                    uploadButton.setVisibility(View.GONE)

                    // if model chosen
                    // execute api
                    if(modelNumberSelected != 99)
                        callAPI(modelNumberSelected)

                }
                else{
                    Toast.makeText(
                        this@MainScreen,
                        "Selected File is too large. This may cause problems. :(",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun callAPI(number: Int) {
        println(number)
        val apiURL = "https://t06twtw4n1.execute-api.us-west-1.amazonaws.com/dev/transform"
        val uploadedImage = findViewById<ImageView>(R.id.uploadedImage)
        val apiedImage = findViewById<ImageView>(R.id.apiedImage)
        val loadingScreen = findViewById<RelativeLayout>(R.id.loadingPanel)

        val byteArrayOutputStream = ByteArrayOutputStream()
        val uploadedBitmap = drawableToBitmap(uploadedImage.drawable)

        if(uploadedImage.drawable != null){
            loadingScreen.visibility = View.VISIBLE
            uploadedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
            val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            println(imageString?.substring(0,100))

            val imageStringWithMIME = "data:image/png;base64," + imageString

            println(imageStringWithMIME.length)

            try {
                val t = Thread {
                    val client = OkHttpClient()

                    val body = JSONObject()
                    body.put("image", imageStringWithMIME)
                    body.put("model_id", number.toString())
                    body.put("load_size", "450")

                    val requestString = body.toString()
                    println(requestString.length)

                    val request: Request = Request.Builder()
                        .url(apiURL)
                        .post(RequestBody.create(MediaType.parse("application/json"), requestString))
                        .addHeader("content-type", "application/json")
                        .build()

                    val response = client.newCall(request).execute()

                    println("Response Code:")
                    println(response.code().toString())
                    if(response.code() != 200){
                        runOnUiThread {
                            Toast.makeText(
                                this@MainScreen,
                                "Something went wrong :/",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingScreen.visibility = View.INVISIBLE
                        }
                    }
                    val respString = response.body()?.string()?.substring(12 )
                    val image = respString?.substring(0, respString?.length - 2)
                    println(respString?.length)
                    println(image?.substring(image?.length - 10))
                    println(image?.substring(20, 25))
                    val imageWithoutMIME = image?.substring(22)

                    val imageBytes = Base64.decode(imageWithoutMIME, 0)

                    val cartoonifiedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)


                    runOnUiThread {
                        if (uploadedBitmap != null) {
                            apiedImage.setImageBitmap(Bitmap.createScaledBitmap(cartoonifiedImage, uploadedBitmap.width, uploadedBitmap.height, false))
                        }
                        apiedImage.visibility = View.VISIBLE
                        loadingScreen.visibility = View.INVISIBLE
                    }
                }

                t.start()

            }
            catch (e: Exception) {
                e.printStackTrace()
            }

        }



    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun executeApi(){
        val apiURL = "https://master-white-box-cartoonization-psi1104.endpoint.ainize.ai/predict"
        val uploadedImage = findViewById<ImageView>(R.id.uploadedImage)
        val apiedImage = findViewById<ImageView>(R.id.apiedImage)
        val loadingScreen = findViewById<RelativeLayout>(R.id.loadingPanel)

//        val matrix = Matrix()
//        uploadedImage.setScaleType(ImageView.ScaleType.MATRIX) //required
//
//        matrix.postRotate(
//            20f,
//            (uploadedImage.getDrawable().getBounds().width() / 2).toFloat(),
//            (uploadedImage.getDrawable().getBounds().height() / 2).toFloat()
//        )

        if(uploadedImage.drawable != null) {
            //uploadedImage.visibility = View.GONE
            var bInput = drawableToBitmap(uploadedImage.drawable as BitmapDrawable)/*your input bitmap*/
            val bOutput: Bitmap
            val matrix = Matrix()
            matrix.preScale(1.0f, 1.0f)
            bOutput = bInput?.let { Bitmap.createBitmap(it, 0, 0, bInput.width, bInput.height, matrix, true) }!!
            apiedImage.setImageBitmap(bOutput)
            apiedImage.visibility = View.VISIBLE
            loadingScreen.visibility = View.VISIBLE
        }

//        val client = OkHttpClient()
//
//        val mediaType: java.awt.PageAttributes.MediaType =
//            java.awt.PageAttributes.MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
//        val body: RequestBody = RequestBody.create(
//            mediaType,
//            "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"file_type\"\r\n\r\nimage\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"source\"; filename=\"♦️jiraiya♦️.jpeg\"\r\nContent-Type: image/jpeg\r\n\r\n\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--"
//        )
//        val request: Request = Builder()
//            .url("https://master-white-box-cartoonization-psi1104.endpoint.ainize.ai/predict")
//            .post(body)
//            .addHeader(
//                "content-type",
//                "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW"
//            )
//            .addHeader("cache-control", "no-cache")
//            .addHeader("postman-token", "4163a5b3-f8df-79bf-68a3-bac59764af5a")
//            .build()
//
//        val response: Response = client.newCall(request).execute()

//        val httpclient: HttpClient = DefaultHttpClient()
//        val response: java.net.http.HttpResponse = httpclient.execute(HttpGet(URL))
//        val statusLine: StatusLine = response.getStatusLine()
//        if (statusLine.getStatusCode() === HttpStatus.SC_OK) {
//            val out = ByteArrayOutputStream()
//            response.getEntity().writeTo(out)
//            val responseString: String = out.toString()
//            out.close()
//            //..more logic
//        } else {
//            //Closes the connection.
//            response.getEntity().getContent().close()
//            throw IOException(statusLine.getReasonPhrase())
//        }



//        val url = URL("https://master-white-box-cartoonization-psi1104.endpoint.ainize.ai/predict")
//        val urlConnection = url.openConnection() as HttpURLConnection
//
//
//        try{
//            urlConnection.doOutput = true
////            urlConnection.setChunkedStreamingMode(0)
//
////            val out = BufferedOutputStream(urlConnection.outputStream) as OutputStream
////            writestream(out)
//
//
//            val inputStream = BufferedInputStream(urlConnection.inputStream)
//            val bitmap = BitmapFactory.decodeStream(inputStream)
//        } catch (e: IOException){
//        } finally{
//            Toast.makeText(this, urlConnection.responseCode.toString(), Toast.LENGTH_SHORT).show()
//            urlConnection.disconnect()
//        }

        Thread {
            val imageToBeCartoonified = returnFile(drawableToBitmap(uploadedImage.drawable))

            val client = OkHttpClient()

//            val mediaType: MediaType? =
//                MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")

//            val body = RequestBody.create(
//                mediaType,
//                "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"file_type\"\r\n\r\nimage\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"source\"; filename=\"$filePath\"\r\nContent-Type: image/jpeg\r\n\r\n\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--"
//            )
            val requestBody: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(
                    "source", imageToBeCartoonified?.absolutePath,
                    RequestBody.create(MediaType.parse("image/jpeg"), imageToBeCartoonified)
                )
                .addFormDataPart("file_type", "image")
                .build()
            val request: Request = Request.Builder()
                .url(apiURL)
                .post(requestBody)
                .build()

            val response: Response = client.newCall(request).execute()

            println(response.code().toString())
            println(response.header("content-length"))
            println(response.header("content-type"))
            println(response.header("access-control-allow-origin"))
            println(response.header("server"))
            println(response.header("x-envoy-upstream-service-time"))
            //println(response.body()?.string())
            println(response.isSuccessful)

//            try {
//                val imageString = response.body()?.string()?.toByteArray(StandardCharsets.UTF_8)
//                val imageEncodedString = Base64.getEncoder().encodeToString(imageString)
//                //val imageString = response.body()?.string()
//                val imageBytes = Base64.getDecoder().decode(imageEncodedString)
//                val size = response.body()?.bytes()?.size ?: 0
//                val image = BitmapFactory.decodeByteArray(response.body()?.bytes(), 0, size)
//
////                val inputStream = BufferedInputStream()
////                val bitmap = BitmapFactory.decodeStream(inputStream)
//            } catch(e: IOException) {
//                e.printStackTrace()
//            }

//            try {
//                val imageString = response.body()?.string()?.toByteArray(StandardCharsets.UTF_8)
//                val imageEncodedString = Base64.getEncoder().encodeToString(imageString)
//                //println(imageEncodedString)
//                //val imageString = response.body()?.string()
//                val imageBytes = Base64.getDecoder().decode(imageEncodedString)
//                val size = imageString?.size ?: 0
//                val image = BitmapFactory.decodeByteArray(imageString, 0, size)
//            } catch(e: IOException) {
//                e.printStackTrace()
//            }



//            val inputStream = BufferedInputStream(response.body()?.bytes())
//            val bitmap = BitmapFactory.decodeStream(inputStream)
            runOnUiThread {
                Toast.makeText(this, "Still needs just a bit more work!", Toast.LENGTH_SHORT).show()
                loadingScreen.visibility = View.INVISIBLE
            }
        }.start()


    }



}