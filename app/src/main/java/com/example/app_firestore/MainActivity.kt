package com.example.app_firestore

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.app_firestore.ui.theme.AppfirestoreTheme
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppfirestoreTheme {
                ImageUploadScreen()
            }
        }
    }

    private fun uploadImage(uri: Uri, context: Context) {
        val fileName = "images/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)
        imageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Toast.makeText(
                        context,
                        "Image uploaded Successfully: $uri",
                        Toast.LENGTH_LONG
                    ).show()
                }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Image Uploading Failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }

    @Composable
    fun ImageUploadScreen() {
        val context = LocalContext.current
        val imageUri = remember { mutableStateOf<Uri?>(null) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri.value = uri
            uri?.let { uploadImage(it, context) }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { launcher.launch("image/*") }) {
                    Text("Select Image")
                }
                Spacer(modifier = Modifier.height(20.dp))
                imageUri.value?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }
}
