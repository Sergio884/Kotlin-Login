package com.example.logint

import android.os.Parcelable
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class PathLocation(var position: LatLng): Parcelable
//data class PathLocation(var lat: String ?=null,var long: String ?=null): Parcelable
