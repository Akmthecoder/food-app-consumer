package com.android.foodieMart.di.modules.app

import com.android.foodieMart.services.MyFirebaseMessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBindingModule {

    @ContributesAndroidInjector
    abstract fun injectMyFirebaseService() : MyFirebaseMessagingService
}