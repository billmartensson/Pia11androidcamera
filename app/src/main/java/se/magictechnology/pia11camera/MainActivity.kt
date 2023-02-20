package se.magictechnology.pia11camera

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var imageuri : Uri

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri

        Log.i("PIA11DEBUG", "VI FICK GALLERI RESULTAT")

        uri?.let {
            val source = ImageDecoder.createSource(this.contentResolver, it)
            val bitmap = ImageDecoder.decodeBitmap(source)

            val theimage = findViewById<ImageView>(R.id.theimage)
            theimage.setImageBitmap(bitmap)

            var storageRef = Firebase.storage.reference
            var imageRef = storageRef.child("androidbild.jpg")

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()

            imageRef.putBytes(data).addOnFailureListener {
                Log.i("PIA11DEBUG", "UPLOAD FAIL")
            }.addOnSuccessListener {
                Log.i("PIA11DEBUG", "UPLOAD OK")
            }

        }
    }

    val getContentcamera = registerForActivityResult(ActivityResultContracts.TakePicture()) {

        if(it == true) {
            Log.i("PIA11DEBUG", "KAMERA OK")
            val source = ImageDecoder.createSource(this.contentResolver, imageuri)
            val bitmap = ImageDecoder.decodeBitmap(source)

            val theimage = findViewById<ImageView>(R.id.theimage)
            theimage.setImageBitmap(bitmap)

        } else {
            Log.i("PIA11DEBUG", "KAMERA EJ OK")

        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.cameraButton).setOnClickListener {
            //dispatchTakePictureIntent()

            val tempfile = getPhotoFile("bilden")
            imageuri = FileProvider.getUriForFile(this, "se.magictechnology.pia11camera.fileprovider", tempfile)

            getContentcamera.launch(imageuri)
        }

        findViewById<Button>(R.id.galleryButton).setOnClickListener {
            getContent.launch("image/*")
        }

        downloadImage()

    }


    fun downloadImage() {
        var storageRef = Firebase.storage.reference
        var imageRef = storageRef.child("androidbild.jpg")

        imageRef.getBytes(1_000_000).addOnSuccessListener {
            var bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

            var theimage = findViewById<ImageView>(R.id.theimage)
            theimage.setImageBitmap(bitmap)

        }.addOnFailureListener {

        }
    }


    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }



    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, 1)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


    }

}