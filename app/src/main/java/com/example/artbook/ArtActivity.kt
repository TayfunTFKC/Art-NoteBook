package com.example.artbook

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PathPermission
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.icu.text.ListFormatter.Width
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Telephony.BaseMmsColumns.READ
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.artbook.databinding.ActivityArtBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class ArtActivity : AppCompatActivity() {
    private lateinit var binding : ActivityArtBinding
    private lateinit var actvivityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerLauncher()


    }
       fun  saveButtonClick(view :View){
           val artName = binding.artNameText.toString()
           val artistName = binding.artistNameText.toString()
           val year = binding.yearText.toString()


           if(selectedBitmap != null){
               val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

               val outputStream = ByteArrayOutputStream()
               smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
               val byteArray = outputStream.toByteArray()


               try {
                   val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                   database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY , artname VARCHAR,artistname VARCHAR, year VARCHAR, image BlOD)")


                   val sqlString = "INSERT INTO arts (artname,artistname,year,image) VALUES (?,?,?,?)"
                   val statement = database.compileStatement(sqlString)
                   statement.bindString(1,artName)
                   statement.bindString(2,artistName)
                   statement.bindString(3,year)
                   statement.bindBlob(4,byteArray)
                   statement.execute()


               }catch (e:Exception){
                   e.printStackTrace()

               }
               finish()

           }


       }
    private  fun makeSmallerBitmap(image :Bitmap, maximumSize : Int) : Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble()/height.toDouble()

        if (bitmapRatio > 1){
            width = maximumSize
            val scaleHeight = width / bitmapRatio
            height = scaleHeight.toInt()

        }else {
            height = maximumSize
            val scaleWidth = height * bitmapRatio
            width = scaleWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height, true)
    }

        fun selectImage(view : View){

            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"PERMİSSİON NEEDED FOR GALLERY", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()

                }else{
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                actvivityResultLauncher.launch(intentToGallery)

            }

    }
    private fun registerLauncher(){
        actvivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){

                val intentFromResult = result.data
                    if(intentFromResult != null){
                        val imageData = intentFromResult.data
                        //binding.imageView.setImageURI(imageData)
                        if(imageData != null) {


                            try {
                                if(Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver, imageData)
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                }else{
                                    selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                }
            }

permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
    if (result){

        val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        actvivityResultLauncher.launch(intentToGallery)

    }else{
        Toast.makeText(this@ArtActivity,"Permisson needed", Toast.LENGTH_LONG).show()
           }

        }
     }

  }
