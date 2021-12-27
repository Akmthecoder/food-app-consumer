package com.android.foodieMart.ui

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.android.foodieMart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_splash)
        startHandler()
    }

    private fun startHandler() {
        Handler().postDelayed({
            FirebaseAuth.getInstance()?.let {
                val currentUser = it.currentUser
                if (currentUser == null) {
                    LoginPresenter.goToHomeAcivity(this)
                } else {
                    setFCM(currentUser)
                    LoginPresenter.getCurrentUser(this, currentUser, null)
                }
            }
        }, 1000)
    }

    private fun setFCM(currentUser: FirebaseUser) {
        currentUser.uid?.let { id ->
            val token: String? = FirebaseInstanceId.getInstance().token
            token?.let {
                val reference =
                    FirebaseDatabase.getInstance().getReference("tokens")
                reference.child(id).setValue(token)
            }
        }
    }
}