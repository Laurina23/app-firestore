package com.example.app_firestore

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.app_firestore.ui.theme.AppfirestoreTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

class MainActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppfirestoreTheme {
                val userList = remember { mutableStateOf<List<User>>(emptyList()) }

                LaunchedEffect(Unit) {
                    startListeningForChanges { users ->
                        userList.value = users
                    }
                }

                Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                    AddUserScreen()
                    Spacer(modifier = Modifier.height(20.dp))
                    DisplayUsers(users = userList.value)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    private fun startListeningForChanges(onResult: (List<User>) -> Unit) {
        listenerRegistration = db.collection("students")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val userList = snapshot.documents.map { document -> document.toObject<User>()!! }
                    onResult(userList)
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }

    private fun addUser(name: String, age: Int) {
        val user = User(name, age)
        db.collection("students")
            .add(user)
            .addOnSuccessListener { docRef ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun deleteUser(name: String, age: Int) {
        val studentsRef = db.collection("students")
        studentsRef.whereEqualTo("name", name)
            .whereEqualTo("age", age)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    studentsRef.document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error deleting document", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents", e)
            }
    }

    private fun updateUser(name: String, newAge: Int) {
        val studentsRef = db.collection("students")
        studentsRef.whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    studentsRef.document(document.id)
                        .update("age", newAge)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating document", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents", e)
            }
    }

    @Composable
    fun AddUserScreen() {
        var name by remember { mutableStateOf("") }
        var age by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = "Enter Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text(text = "Enter Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    val ageInt = age.toIntOrNull()
                    if (ageInt != null) {
                        addUser(name, ageInt)
                    } else {
                        Log.w(TAG, "Invalid age input")
                    }
                }) {
                    Text("Add User")
                }
                Button(onClick = {
                    val ageInt = age.toIntOrNull()
                    if (ageInt != null) {
                        deleteUser(name, ageInt)
                    } else {
                        Log.w(TAG, "Invalid age input")
                    }
                }) {
                    Text("Delete User")
                }
            }
            Button(onClick = {
                val newAgeInt = age.toIntOrNull()
                if (newAgeInt != null) {
                    updateUser(name, newAgeInt)
                } else {
                    Log.w(TAG, "Invalid age input")
                }
            }) {
                Text("Update User")
            }
        }
    }

    @Composable
    fun DisplayUsers(users: List<User>) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(20.dp)
        ) {
            items(users) { user ->
                UserCard(user = user)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    @Composable
    fun UserCard(user: User) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F0F0)
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Name: ${user.name}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Age: ${user.age}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

data class User(
    val name: String = "",
    val age: Int = 0
)
