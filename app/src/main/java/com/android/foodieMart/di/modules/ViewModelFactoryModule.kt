package com.android.foodieMart.di.modules

import androidx.lifecycle.ViewModelProvider
import com.android.foodieMart.di.viewmodelfactory.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: ViewModelProviderFactory): ViewModelProvider.Factory
}
