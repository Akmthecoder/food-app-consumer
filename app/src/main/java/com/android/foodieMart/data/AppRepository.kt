package com.android.foodieMart.data

import com.android.foodieMart.data.models.response.NotificationResponse
import com.android.foodieMart.data.network.AppApiService
import io.reactivex.Single
import javax.inject.Inject

class AppRepository @Inject constructor(private val apiService: AppApiService) {

    fun getNotifications(): Single<NotificationResponse> {
        return apiService.getNotifications()
    }
}