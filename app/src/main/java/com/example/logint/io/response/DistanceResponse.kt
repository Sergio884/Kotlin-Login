package com.example.logint.io.response

import com.example.logint.model.Step

data class DistanceResponse(
    val distance:Int,
    val price:Float,
    val steps: List<Step>,
    val time : String
)
