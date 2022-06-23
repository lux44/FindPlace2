package com.lux.tpkakaosearch.model

data class NaverUserInfoResponse(
    var resultcode:String,
    var message:String,
    var response:NidUser
)

data class NidUser constructor(
    var id:String,
    var email:String
)
