package com.example.blinkitadmin.api


import com.example.blinkitadmin.models.notification.Notification
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterFace {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAAee4ZibU:APA91bFmxu3Fmn2XYF0QNysGl0U8GcazoRBpJo8GDnANICiZVx89icy6f6jYtRLEYgbCShyvKexqrh4u2KkjzTMQKN0DiOmsHmQthdzRj5UTWQxurqt-gvk8dOa5DC0CbsMkVsR8uIxo"
    )
    @POST("fcm/send")
    fun sendNotification(@Body notification : Notification) : Call<Notification>
}
