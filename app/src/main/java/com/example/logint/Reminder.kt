package com.example.logint

data class Reminder(    // Aqui podemos añadir lo que queramos, por ejemplo tiempos y cosas asi
    var key: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var time: String = "",
)