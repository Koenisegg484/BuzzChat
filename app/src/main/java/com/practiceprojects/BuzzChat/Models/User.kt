package com.practiceprojects.BuzzChat.Models

class User {
    var userid : String ?= null
    var name:String ?= null
    var username : String ?= null
    var email : String ?= null
    var birthdate : String ?= null
    var profileUrl : String ?= null

    constructor()
    constructor(
        name: String?,
        username: String?,
        email: String?,
        birthdate: String?,
        profileurl:String
    ) {
        this.name = name
        this.username = username
        this.email = email
        this.birthdate = birthdate
        this.profileUrl
    }


}