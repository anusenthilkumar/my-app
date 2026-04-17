package com.example.guardianangel

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // 🔥 Always try login (no condition)
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    Log.d("FIREBASE_SUCCESS", "UID: $uid")
                    Toast.makeText(this, "Logged in: $uid", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("FIREBASE_ERROR", "Error: ", task.exception)
                    Toast.makeText(this, "Login failed!", Toast.LENGTH_LONG).show()
                }
            }
    }
}