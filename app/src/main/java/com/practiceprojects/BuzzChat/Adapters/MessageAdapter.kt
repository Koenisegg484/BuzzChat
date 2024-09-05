package com.practiceprojects.BuzzChat.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.practiceprojects.BuzzChat.Models.Message
import com.practiceprojects.BuzzChat.R
import com.practiceprojects.BuzzChat.databinding.DeleteBinding
import com.practiceprojects.BuzzChat.databinding.RecieveMsgLayoutBinding
import com.practiceprojects.BuzzChat.databinding.SendMsgLayoutBinding

class MessageAdapter (var context: Context, messages:ArrayList<Message>, senderRoom:String, receiverRoom:String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

        lateinit var messages : ArrayList<Message>
        val ITEM_SENT = 1
        val ITEM_RECIEVE = 2
        var senderRoom: String?
        var recieverRoom: String?

        inner class SentMsgHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
            var binder : SendMsgLayoutBinding = SendMsgLayoutBinding.bind(itemView)
        }
        inner class ReceiveMsgHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
            var binder : RecieveMsgLayoutBinding = RecieveMsgLayoutBinding.bind(itemView)
        }

        init{
            if(messages != null){
                this.messages = messages
            }
            this.senderRoom = senderRoom
            this.recieverRoom = receiverRoom
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == ITEM_SENT){
            val view:View = LayoutInflater.from(context).inflate(R.layout.send_msg_layout, parent,false)
            SentMsgHolder(view)
        }else{
            val view:View = LayoutInflater.from(context).inflate(R.layout.recieve_msg_layout, parent,false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
        val msg = messages[position]
        return if(FirebaseAuth.getInstance().uid == msg.senderId){
            ITEM_SENT
        }else{
            ITEM_RECIEVE
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        if(holder.javaClass == SentMsgHolder::class.java){
            val viewholder = holder as SentMsgHolder
            if(msg.message.equals("photo")){
                viewholder.binder.image.visibility = View.VISIBLE
//                viewholder.binder.image.visibility = View.GONE
                Glide.with(context).load(msg.imageurl).placeholder(R.drawable.image).into(viewholder.binder.image)
            }
            viewholder.binder.message.text = msg.message
            viewholder.itemView.setOnLongClickListener{
                val view = LayoutInflater.from(context).inflate(R.layout.delete, null)
                val binding:DeleteBinding = DeleteBinding.bind(view)

                val dialog : AlertDialog = AlertDialog.Builder(context).setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener{
                    msg.message = "This message was deleted"
                    msg.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("CHATS")
                            .child(senderRoom!!)
                            .child("MESSAGES")
                            .child(it1!!).setValue(msg)
                    }
                    msg.messageId.let { it2 ->
                        FirebaseDatabase.getInstance().reference.child("CHATS")
                            .child(recieverRoom!!)
                            .child("MESSAGES")
                            .child(it2!!).setValue(msg)
                    }
                    dialog.dismiss()
                }

                binding.meonly.setOnClickListener{
                    msg.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("CHATS")
                            .child(senderRoom!!)
                            .child("MESSAGES")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()
                }

                binding.cancel.setOnClickListener{
                    dialog.dismiss()
                }

                dialog.show()

                false
            }
        }
        else{

            val viewholder = holder as ReceiveMsgHolder
            if(msg.message.equals("photo")){
                viewholder.binder.image.visibility = View.VISIBLE
//                viewholder.binder.image.visibility = View.GONE
                Glide.with(context).load(msg.imageurl).placeholder(R.drawable.image).into(viewholder.binder.image)
            }

            viewholder.binder.message.text = msg.message
            viewholder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete, null)
                val binding: DeleteBinding = DeleteBinding.bind(view)

                val dialog: AlertDialog = AlertDialog.Builder(context).setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    msg.message = "This message was deleted"
                    msg.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("CHATS")
                            .child(senderRoom!!)
                            .child("MESSAGES")
                            .child(it1!!).setValue(msg)
                    }
                    msg.messageId.let { it2 ->
                        FirebaseDatabase.getInstance().reference.child("CHATS")
                            .child(recieverRoom!!)
                            .child("MESSAGES")
                            .child(it2!!).setValue(msg)
                    }
                    dialog.dismiss()
                }

                binding.meonly.setOnClickListener {
                    msg.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("CHATS")
                            .child(senderRoom!!)
                            .child("MESSAGES")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()
                }

                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
                false
            }
        }
    }
}