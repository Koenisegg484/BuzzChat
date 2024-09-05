package com.practiceprojects.BuzzChat.authentication

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.practiceprojects.BuzzChat.UsersChattingPageActivity
import com.practiceprojects.BuzzChat.databinding.ActivityMainBinding
import com.practiceprojects.BuzzChat.databinding.ActivityUserDetailsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.practiceprojects.BuzzChat.Models.User
import java.util.UUID

class MainActivity : AppCompatActivity() {

    val myCalendar = Calendar.getInstance()
    lateinit var auth: FirebaseAuth;
    lateinit var db : FirebaseDatabase;
    lateinit var storageReference: FirebaseStorage;
    lateinit var binding: ActivityMainBinding;
    lateinit var binding1 : ActivityUserDetailsBinding;
    lateinit var selectedImageUri: Uri
    var useremail :String = ""
    var act = false
    var imageflag : Boolean = false


    public override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            var intent = Intent(this, UsersChattingPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding1 = ActivityUserDetailsBinding.inflate(layoutInflater)
        auth = Firebase.auth
        db = FirebaseDatabase.getInstance()
        storageReference = FirebaseStorage.getInstance()
        setContentView(binding.root)
        enableEdgeToEdge()

        var userpassword :String = ""
        var confirmPassword :String = ""
        var birthdate :String ?= null
        var name :String ?= null
        var username :String ?= null
        var profileimage : String = ""

        binding.touserdetailspage.setOnClickListener{
            useremail = binding.useremail.text.toString()
            userpassword = binding.userPassword.text.toString()
            confirmPassword = binding.confirmPassword.text.toString()
            if(checkCredentials(useremail, userpassword, confirmPassword)){
                act = true
                setContentView(binding1.root)
            }
        }

        binding.loginpage.setOnClickListener{
            var intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


//        For the user details page
//        DatePicker
        var date =  DatePickerDialog.OnDateSetListener{ datePicker: DatePicker, year: Int, month: Int, day: Int ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH,month);
            myCalendar.set(Calendar.DAY_OF_MONTH,day);
            birthdate = updateDate()
        }
        binding1.birthdate.setOnClickListener{
            DatePickerDialog(
                this@MainActivity,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding1.profilepic.setOnClickListener{
            var intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding1.signupbutton.setOnClickListener {
            name = binding1.name.text.toString()
            username = binding1.username.text.toString()
            birthdate = binding1.birthdate.text.toString()

            if (imageflag) {
                val randomId: UUID = UUID.randomUUID()
                val imageName: String = "ProfilePictures/$randomId.jpg"
                val imageRef: StorageReference = storageReference.getReference(imageName)

                imageRef.putFile(selectedImageUri).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        profileimage = uri.toString()
                    }.addOnFailureListener {
                        // Handle any errors with retrieving the download URL
                        println("Error: ${it.message}")
                    }
                }.addOnFailureListener {
                    // Handle any errors with uploading the image
                    println("Error: ${it.message}")
                }

                val newUser = User(
                    name,
                    username,
                    useremail,
                    birthdate,
                    profileimage
                )
                registerUser(newUser, userpassword, profileimage) { success, message ->
                    if (success) {
                        println("Success: $message")
                    } else {
                        println("Error: $message")
                    }
                }
            } else {
                profileimage = "No image"

                val newUser = User(
                    name,
                    username,
                    useremail,
                    birthdate,
                    profileimage
                )
                registerUser(newUser, userpassword, profileimage) { success, message ->
                    if (success) {
                        println("Success: $message")
                    } else {
                        println("Error: $message")
                    }
                }
            }

        }
    }

    override fun onBackPressed() {
        if (act){
            setContentView(binding.root)
            binding.userPassword.setText("")
            binding.confirmPassword.setText("")
            act = false
        }else{
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if(requestCode == 1 && resultCode == RESULT_OK && data.data != null){
                selectedImageUri = data.data!!
                imageflag = true
                selectedImageUri.let {
                    Glide.with(this@MainActivity).load(selectedImageUri).into(binding1.profilepic)
                }
            }
        }
    }

    private fun checkCredentials(email:String, pass : String, confirmPass:String) : Boolean{
        var msg : String = ""
        // Basic checks
        if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            msg = "All fields are required."
            binding.errmsg.text = msg
            binding.errmsg.visibility = View.VISIBLE
            return false
        }
        if (pass != confirmPass) {
            msg = "Passwords do not match."
            binding.errmsg.text = msg
            binding.errmsg.visibility = View.VISIBLE
            return false
        }
        if (pass.length < 8) {
            msg = "Password should be at least 8 characters long."
            binding.errmsg.text = msg
            binding.errmsg.visibility = View.VISIBLE
            return false
        }

        return true
    }
    private fun updateDate():String{
        var dateformat = "MM/dd/yyyy"
        var simpledateFormat = SimpleDateFormat(dateformat, Locale.US)
        binding1.birthdate.setText(simpledateFormat.format(myCalendar.time))
        return binding1.birthdate.text.toString()
    }

    private fun addUserDetails(user: User, uid: String,profileUrl : String, callback: (Boolean, String) -> Unit){
        var userMap = hashMapOf(
            "name" to user.name,
            "username" to user.username,
            "email" to user.email,
            "birthdate" to user.birthdate,
            "profileUrl" to user.profileUrl
        )

        db.getReference().child("USERS").child(uid).setValue(userMap)
        db.getReference().child("USERS").child(uid).child("profileUrl").setValue(profileUrl)
    }

    fun registerUser(newUser : User, password: String, profileUrl: String, callback: (Boolean, String) -> Unit) {

        var email : String= newUser.email.toString()

        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener {task ->
            if(task.isSuccessful){
                val signInMethods = task.result?.signInMethods
                if (signInMethods.isNullOrEmpty()){
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { createTask ->
                        if (createTask.isSuccessful) {
                            auth.uid?.let {
                                addUserDetails(newUser, it, profileUrl) { success, message ->
                                    if (success) {
                                        auth.signInWithEmailAndPassword(email, password);
                                        println("Success: $message")
                                    } else {
                                        println("Error: $message")
                                    }
                                }
                            }
                            callback(true, "Registration successful.")
                        } else {
                            callback(false, "Registration failed: ${createTask.exception?.message}")
                        }
                    }
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                }
            }
        }
    }

}