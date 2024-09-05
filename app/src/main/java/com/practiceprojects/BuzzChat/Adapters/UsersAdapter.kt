package com.practiceprojects.BuzzChat.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        Glide.with(context).load(user.profileUrl).placeholder(R.drawable.ghost).into(holder.binding.profiledp)
        holder.itemView.setOnClickListener{
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("oname", user.name)
            intent.putExtra("ouid", user.userid)
            intent.putExtra("profiledp", user.profileUrl)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userlist.size


}