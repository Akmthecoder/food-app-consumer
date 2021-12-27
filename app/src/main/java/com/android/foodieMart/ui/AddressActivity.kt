package com.android.foodieMart.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.foodieMart.R
import com.android.foodieMart.data.request.User
import com.android.foodieMart.dialog.ViewDialog
import com.android.foodieMart.network.firebase.Connection
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_address.*
import kotlinx.android.synthetic.main.app_header.*

class AddressActivity : AppCompatActivity() {
    private lateinit var user: User
    private lateinit var loadingDialog: ViewDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)
        initiateViews()
        intent?.let {
            val userStr = it.getStringExtra(Connection.USERS)
            userStr?.let {
                user = Gson().fromJson(it, User::class.java)
                et_address.setText(user.address)
            }
        }
    }

    private fun initiateViews() {
        loadingDialog = ViewDialog(this)

        tvHeader.setText("Address Details")

        ivBackHeader.setOnClickListener {
            onBackPressed()
        }

        btn_save.setOnClickListener {
            if (et_address.text.isNotEmpty()) {
                loadingDialog.showDialog()
                user.address = et_address.text.toString()
                val userStr = Gson().toJson(user)
                FirebaseDatabase.getInstance().reference.child(Connection.USERS).child(user.id)
                    .setValue(userStr).addOnCompleteListener {
                        loadingDialog.hideDialog()
                        if (it.isSuccessful) {
                            val intent = Intent()
                            intent.putExtra(Connection.USERS, userStr)
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
            }
        }
    }
}