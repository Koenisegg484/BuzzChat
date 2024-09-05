package com.practiceprojects.BuzzChat.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.practiceprojects.BuzzChat.ChatActivity
import com.practiceprojects.BuzzChat.Models.User
import com.practiceprojects.BuzzChat.R
import com.practiceprojects.BuzzChat.databinding.UserCardBinding

class UsersAdapter (var context:Context, var userlist: ArrayList<User>) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val binding : UserCardBinding = UserCardBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersAdapter.ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: UsersAdapter.ViewHolder, position: Int) {
        val user = userlist[position]
        holder.binding.chatusername.text = user.username
        FirebaseDatabase.getInstance().reference.child("CHATS")
            .child(user.userid + FirebaseAuth.getInstance().currentUser!!.uid)
            .child("lastmsg")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    // Get the last message from the DataSnapshot
                    val lastMessage = dataSnapshot.getValue(String::class.java)
                    // Set the last message to the TextView
                    holder.binding.lastmessage.text = lastMessage ?: "No messages"
                } else {
                    holder.binding.lastmessage.text = "No messages"
                }
            }

        Glide.with(context).load(user.profileUrl).placeholder(R.drawable.ghost).into(holder.binding.profiledp)
        holder.itemView.setOnClickListener{
            println("User ID: ${user.userid}")
            println("User Name: ${user.name}")

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("oname", user.name)
            intent.putExtra("ouid", user.userid)
            intent.putExtra("profiledp", user.profileUrl)
            context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int = userlist.size
}