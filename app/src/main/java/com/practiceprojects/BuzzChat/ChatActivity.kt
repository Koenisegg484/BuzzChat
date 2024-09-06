package com.practiceprojects.BuzzChat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
    var selectedImage: Uri?= null

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
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val status = snapshot.getValue(String::class.java)
                        if(status == "offline"){
                            binding.status.visibility = View.GONE
                        }else{
                            binding.status.visibility = View.VISIBLE
                            binding.status.text = "Online"
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
                override fun onCancelled(error: DatabaseError) {}
            })

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

        binding.iconCamera.setOnClickListener {
            val intent = Intent(this@ChatActivity, CameraLayout::class.java)
            startActivity(intent)
        }

        binding.sendbutton.setOnClickListener {
            val msgtxt: String = binding.editTextMessage.text.toString()

            if (msgtxt.isNotEmpty() || selectedImage != null) {  // Ensure at least a message or image is present
                val date = Date()
                val message = Message(msgtxt, senderUid, date.time)  // Create message object
                binding.editTextMessage.setText("")  // Clear the message input field
                val randomkey = reference.push().key
                val lastMsgObj = HashMap<String, Any>()
                lastMsgObj["lastmsg"] = msgtxt.ifEmpty { "photo" }
                lastMsgObj["msgtime"] = date.time

                // If an image is selected
                if (selectedImage != null) {
                    val imagename: String = "InMessageImages/" + UUID.randomUUID() + ".jpg"
                    val ref = storageReference.child("CHATS").child(imagename)

                    // Upload the image
                    ref.putFile(selectedImage!!).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            ref.downloadUrl.addOnSuccessListener { uri ->
                                val filepath = uri.toString()
                                message.imageurl = filepath  // Set the image URL in the message

                                // Update the last message and time after image is uploaded
                                reference.child("CHATS").child(senderRoom).updateChildren(lastMsgObj)
                                reference.child("CHATS").child(receverRoom).updateChildren(lastMsgObj)

                                // Send the message with image URL
                                reference.child("CHATS").child(senderRoom)
                                    .child("MESSAGES")
                                    .child(randomkey!!)
                                    .setValue(message)
                                    .addOnSuccessListener {
                                        reference.child("CHATS").child(receverRoom)
                                            .child("MESSAGES")
                                            .child(randomkey)
                                            .setValue(message)
                                    }
                            }
                        }
                    }
                    selectedImage = null  // Clear the selected image after sending
                    binding.imgtosend.visibility = View.GONE
                } else {
                    // If no image is selected, send the text message immediately
                    reference.child("CHATS").child(senderRoom).updateChildren(lastMsgObj)
                    reference.child("CHATS").child(receverRoom).updateChildren(lastMsgObj)

                    reference.child("CHATS").child(senderRoom)
                        .child("MESSAGES")
                        .child(randomkey!!)
                        .setValue(message)
                        .addOnSuccessListener {
                            reference.child("CHATS").child(receverRoom)
                                .child("MESSAGES")
                                .child(randomkey)
                                .setValue(message)
                        }
                }
            }
        }


    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 25 && resultCode == RESULT_OK && data != null){
            if(data.data != null){
                selectedImage = data.data
                binding.imgtosend.visibility = View.VISIBLE
                Glide.with(this@ChatActivity).load(selectedImage).placeholder(R.drawable.image).into(binding.imgtosend)
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