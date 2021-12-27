package com.android.foodieMart.di.modules

import com.android.foodieMart.di.modules.app.*
import dagger.Module

@Module(
    includes = [
        ActivityBindingModule::class,
        MainActivityBindingModule::class,
        NetworkModule::class,
        ServiceBindingModule::class,
        AppViewModelModule::class
    ]
)
class AppModule {


}