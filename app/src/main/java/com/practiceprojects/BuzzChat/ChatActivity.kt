package com.practiceprojects.BuzzChat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.practiceprojects.BuzzChat.Adapters.MessageAdapter
import com.practiceprojects.BuzzChat.Models.Message
import com.practiceprojects.BuzzChat.databinding.ActivityChatBinding
import com.practiceprojects.BuzzChat.databinding.UserCardBinding
import java.util.Calendar
import java.util.Date
import java.util.UUID


class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    lateinit var messages : ArrayList<Message>
    lateinit var senderRoom:String
    lateinit var receverRoom :String

    var database:FirebaseDatabase = FirebaseDatabase.getInstance()
    var reference = database.reference

    var storage:FirebaseStorage = FirebaseStorage.getInstance()
    var storageReference:StorageReference = storage.reference

    lateinit var senderUid:String
    lateinit var receiverUid:String
    var adapter : MessageAdapter?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        supportActionBar!!.hide()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messages = ArrayList<Message>()

        val name = intent.getStringExtra("oname")
        receiverUid = intent.getStringExtra("ouid").toString()
        senderUid = FirebaseAuth.getInstance().uid.toString()
        val profile = intent.getStringExtra("profiledp")

        binding.ousername.text = intent.getStringExtra("oname")
        Glide.with(this@ChatActivity)
            .load(profile)
            .placeholder(R.drawable.ghost)
            .into(binding.oprofiledp)

        binding.backbutton.setOnClickListener { finish() }

        reference.child("STATUS").child(receiverUid)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val status = snapshot.getValue(String::class.java)
                        if(status == "offline"){
                            binding.status.visibility = View.GONE
                        }else{
                            binding.status.visibility = View.VISIBLE
                            binding.status.setText("Online")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        senderRoom = senderUid + receiverUid
        receverRoom = receiverUid + senderUid
        adapter = MessageAdapter(this, messages, senderRoom, receverRoom)

        binding.msgRv.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding.msgRv.adapter = adapter
        reference.child("CHATS")
            .child(senderRoom)
            .child("MESSAGES")
            .addValueEventListener(object : ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for(snap in snapshot.children){
                        val message : Message? = snap.getValue(Message::class.java)
                        message!!.messageId = snap.key.toString()
                        messages.add(message)
                        adapter!!.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        binding.sendbutton.setOnClickListener{
            var msgtxt:String = binding.editTextMessage.text.toString()
            if(!msgtxt.isEmpty()) {

                val date = Date()
                val message = Message(msgtxt, senderUid, date.time)

                binding.editTextMessage.setText("")
                val randomkey = reference.push().key
                val lastMsgObj = HashMap<String, Any>()
                lastMsgObj["lastmsg"] = message.message!!
                lastMsgObj["msgtime"] = date.time

                reference.child("CHATS").child(senderRoom).updateChildren(lastMsgObj)
                reference.child("CHATS").child(receverRoom).updateChildren(lastMsgObj)

                reference.child("CHATS").child(senderRoom)
                    .child("MESSAGES")
                    .child(randomkey!!)
                    .setValue(message)
                    .addOnSuccessListener {
                        reference.child("CHATS").child(receverRoom)
                            .child("MESSAGES")
                            .child(randomkey!!)
                            .setValue(message)
                    }
            }
        }

        binding.attachbutton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        val handler = Handler()
        binding.editTextMessage.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                reference.child("STATUS")
                    .child(senderUid)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }
            var userStoppedTyping = Runnable {
                reference.child("STATUS").child(senderUid).setValue("Online")
            }
        })

//        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == 45 && requestCode == RESULT_OK && data != null){
            if(data.data != null){
                val selectedImage = data.data
                val imagename:String = "InMessageImages/" + UUID.randomUUID() + ".jpg"
                var ref = storageReference.child("CHATS")
                    .child(imagename)
                ref.putFile(selectedImage!!)
                    .addOnCompleteListener {task ->
                        if(task.isSuccessful){
                            ref.downloadUrl.addOnSuccessListener { uri ->
                                val filepath = uri.toString()
                                val msgTxt : String = binding.editTextMessage.text.toString()
                                val date = Date()

                                val msg = Message(msgTxt, senderUid, date.time)
                                msg.message = "photo"
                                msg.imageurl = filepath
                                binding.editTextMessage.setText("")
                                val randomkey = reference.push().key
                                val lastMsgObj = HashMap<String, Any>()
                                lastMsgObj["lastmsg"] = msg.message!!
                                lastMsgObj["msgtime"] = date.time

                                reference.child("CHATS").child(senderRoom).updateChildren(lastMsgObj)
                                reference.child("CHATS").child(receverRoom).updateChildren(lastMsgObj)

                                reference.child("CHATS").child(senderRoom)
                                    .child("MESSAGES")
                                    .child(randomkey!!)
                                    .setValue(msg)
                                    .addOnSuccessListener {
                                        reference.child("CHATS").child(receverRoom)
                                            .child("MESSAGES")
                                            .child(randomkey)
                                            .setValue(msg)
                                    }
                            }
                        }
                    }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("STATUS").child(currentId!!).setValue("offline")
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("STATUS").child(currentId!!).setValue("Online")
    }
}