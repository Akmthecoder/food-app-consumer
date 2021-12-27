package com.android.foodieMart.utils

import android.app.Activity
import android.content.Intent
import com.android.foodieMart.ui.LoginActivity
import com.google.firebase.auth.FirebaseAuth


object CheckForLogin {
    fun checkUserLoginOrNot(context: Activity): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            context.startActivity(Intent(context, LoginActivity::class.java))
            (context as Activity).finish()
        }
        return currentUser != null
    }
}

