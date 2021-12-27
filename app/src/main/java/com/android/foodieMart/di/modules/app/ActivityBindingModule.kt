package com.android.foodieMart.di.modules.app

import com.android.foodieMart.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector(modules = [MainActivityBindingModule::class])
    abstract fun injectMainActivity(): MainActivity

}
