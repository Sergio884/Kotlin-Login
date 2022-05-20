package com.example.logint
import android.app.Application

class GlobalClass : Application() {

    private var banderaSOS = 0


    companion object {
        var course = 0
    }

    public fun getBandera(): Int{
        return banderaSOS
    }

    public fun setBandera(banderaSOS: Int){
        this.banderaSOS=banderaSOS
    }

    public fun getCourse(): Int{
        return course
    }

    public fun setCourse(courses: Int){
        course=courses
    }

}