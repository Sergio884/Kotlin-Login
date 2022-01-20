package com.example.logint
import android.app.Application

class GlobalClass : Application() {

    private var banderaSOS = 0


    public fun getBandera(): Int{
        return banderaSOS
    }

    public fun setBandera(banderaSOS: Int){
        this.banderaSOS=banderaSOS
    }


}