package com.practiceprojects.BuzzChat.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.practiceprojects.BuzzChat.R
import com.practiceprojects.BuzzChat.UsersChattingPageActivity
import com.practiceprojects.BuzzChat.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding;
    lateinit var auth : FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar!!.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        var useremail = binding.useremail.text.toString()
        var userpassword = binding.userPassword.text.toString()

        binding.loginbutton.setOnClickListener{
            auth.signInWithEmailAndPassword(useremail, userpassword).addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Message", "signInWithEmail:success")
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Message", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

            }
        }
    }
}