package com.practiceprojects.BuzzChat.Models

class Message {
    lateinit var messageId : String
    lateinit var message : String
    lateinit var senderId : String
    lateinit var recieverId : String
    lateinit var imageurl : String
    var timestamp: Long = 0

    constructor()

    constructor(
        message :String,
        senderId :String,
        timestamp: Long
    ){
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
    }
}