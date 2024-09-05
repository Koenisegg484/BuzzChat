package com.practiceprojects.BuzzChat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.practiceprojects.BuzzChat.Adapters.UsersAdapter
import com.practiceprojects.BuzzChat.Models.User
import com.practiceprojects.BuzzChat.databinding.ActivityLoginBinding
import com.practiceprojects.BuzzChat.databinding.ActivityUsersChattingPageBinding

class UsersChattingPageActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var database: FirebaseDatabase
    lateinit var reference: DatabaseReference

    lateinit var users:ArrayList<User>
    lateinit var usersAdapter: UsersAdapter
    lateinit var dialog: ProgressDialog
    lateinit var binding: ActivityUsersChattingPageBinding

    lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar!!.hide()
        binding = ActivityUsersChattingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        database = FirebaseDatabase.getInstance()
        reference = database.reference

        dialog = ProgressDialog(this@UsersChattingPageActivity)
        dialog.setMessage("Loading Chats...")
        dialog.setCancelable(false)

        users = ArrayList<User>()
        usersAdapter = UsersAdapter(this@UsersChattingPageActivity, users)

        val layoutmanager :LinearLayoutManager = LinearLayoutManager(this@UsersChattingPageActivity)
        binding.userlistRv.layoutManager = layoutmanager
        reference.child("USERS").child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.userlistRv.adapter = usersAdapter
        reference.child("USERS")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    users.clear()
                    for(snaps in snapshot.children){
                        val user1 : User? = snaps.getValue(User::class.java)
                        if (user1 != null) {
                            if(!user1.userid.equals(auth.uid)){
                                users.add(user1)
                            }
                        }
                    }
                    usersAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        binding.materialToolbar.setOnClickListener {
            binding.searchlayout.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        val currentId = auth.uid
        reference.child("STATUS").child(currentId!!).setValue("Offline")
    }
    override fun onResume() {
        super.onResume()
        val currentId = auth.uid
        reference.child("STATUS").child(currentId!!).setValue("Online")
    }
}