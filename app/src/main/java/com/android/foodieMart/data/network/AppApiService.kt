package com.android.foodieMart.data.network

import com.android.foodieMart.data.models.response.NotificationResponse
import io.reactivex.Single
import retrofit2.http.GET

interface AppApiService {

    @GET("v1/notification/fetch/list")
    fun getNotifications(): Single<NotificationResponse>

}