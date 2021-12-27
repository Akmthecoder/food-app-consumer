package com.android.foodieMart.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.android.foodieMart.data.request.User
import com.android.foodieMart.dialog.ViewDialog
import com.android.foodieMart.network.firebase.Connection
import com.android.foodieMart.network.firebase.FirebaseMethods
import com.android.foodieMart.network.firebase.RequestCallback
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson


object LoginPresenter {

    fun getCurrentUser(context: Context, currentUser: FirebaseUser, dialog: ViewDialog?) {
        val userId = currentUser.uid
        FirebaseMethods.singleValueEventChild(Connection.USERS, userId, object : RequestCallback {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    dialog?.hideDialog()
                    val userStr = dataSnapshot.getValue(String::class.java)
                    val user = Gson().fromJson(userStr, User::class.java)
                    if (user.username.isNotEmpty()) {
                        goToHomeAcivity(context)
                    } else {
                        userStr?.let {
                            goToDetailsActivity(context, it)
                        }
                    }
                } else {
                    FirebaseMethods.singleValueEventChild(
                        Connection.SERVICE,
                        userId,
                        object : RequestCallback {
                            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    dataSnapshot.getValue(Boolean::class.java)?.let {
                                        if (it) {
                                            createUserDatabase(context, userId, currentUser, dialog)
                                        } else {
                                            dialog?.hideDialog()
                                            Toast.makeText(
                                                context,
                                                "Seller cannot login in customer application",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            finishActivity(context)
                                        }
                                    }
                                } else {
                                    addToServiceUser(context, currentUser, dialog)
                                }
                            }
                        })
                }
            }
        })
    }

    private fun finishActivity(context: Context) {
        android.os.Handler().postDelayed(Runnable { (context as Activity).finish() }, 2000)
    }

    private fun createUserDatabase(
        context: Context,
        userId: String,
        currentUser: FirebaseUser,
        dialog: ViewDialog?
    ) {
        val user = User().apply {
            this.id = userId
            this.email = if (currentUser.email == null) "" else currentUser.email.toString()
            this.phone_number =
                if (currentUser.phoneNumber == null) "" else currentUser.phoneNumber.toString()
        }
        val userStr = Gson().toJson(user)
        FirebaseDatabase.getInstance().reference.child(Connection.USERS).child(userId)
            .setValue(userStr).addOnCompleteListener {
                if (it.isSuccessful) {
                    dialog?.hideDialog()
                    goToDetailsActivity(context, userStr)
                } else {
                    dialog?.hideDialog()
                }
            }
    }

    private fun addToServiceUser(context: Context, currentUser: FirebaseUser, dialog: ViewDialog?) {
        FirebaseDatabase.getInstance().reference.child(Connection.SERVICE).child(currentUser.uid)
            .setValue(true).addOnCompleteListener {
                if (it.isSuccessful) {
                    createUserDatabase(context, currentUser.uid, currentUser, dialog)
                } else {
                    dialog?.hideDialog()
                }
            }
    }

    private fun goToDetailsActivity(context: Context, user: String) {
        val intent = Intent(context, UserDetailsActivity::class.java)
        intent.putExtra(Connection.USERS, user)
        context.startActivity(intent)
        (context as Activity).finish()
    }

    fun goToHomeAcivity(context: Context) {
        context.startActivity(Intent(context, HomeActivity::class.java))
        (context as Activity).finish()
    }
}