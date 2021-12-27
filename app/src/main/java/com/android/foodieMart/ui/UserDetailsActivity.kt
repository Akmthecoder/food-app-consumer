package com.android.foodieMart.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.foodieMart.R
import com.android.foodieMart.data.request.User
import com.android.foodieMart.dialog.ViewDialog
import com.android.foodieMart.network.firebase.Connection
import com.android.foodieMart.utils.CommonUtils
import com.android.foodieMart.utils.CommonUtils.isValidEmailId
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_signup.*

class UserDetailsActivity : AppCompatActivity() {
    var userString:String?=""
    lateinit var viewDialog: ViewDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        viewDialog=  ViewDialog(this)
        userString = intent.getStringExtra(Connection.USERS)
        val user = Gson().fromJson<User>(userString, User::class.java)
        if (user.phone_number!=""){
            etPhoneNo.isEnabled=false
            etPhoneNo.setText(user.phone_number)
        }
        if (user.email!=""){
            tiEtEmail.isEnabled=false
            tiEtEmail.setText(user.email)
        }

        bt_proceed.setOnClickListener {
            doSignUp()
        }
    }

    private fun doSignUp() {
        if (doValidations()) {
            viewDialog.showDialog()
            val email = tiEtEmail.text.toString()
            val phoneNo = etPhoneNo.text.toString()
            val userName = tiEtUserName.text.toString()
            val user = Gson().fromJson(userString, User::class.java)
            user.phone_number=phoneNo
            user.email=email
            user.username=userName
            var updatedUserStr= Gson().toJson(user)
            FirebaseDatabase.getInstance().reference.child(Connection.USERS).child(user.id).setValue(updatedUserStr).addOnCompleteListener {
                if (it.isSuccessful){
                    viewDialog.hideDialog()
                    startActivity(Intent(this,HomeActivity::class.java))
                }else{
                    viewDialog.hideDialog()
                }
            }
        }
    }

    private fun doValidations(): Boolean {
        if (tiEtUserName.text.toString() == "") {
            CommonUtils.showSnackBar(this, "User Name is Mandatory", root)
            return false
        } else if (tiEtEmail.isEnabled && tiEtEmail.text.toString() == "") {
            CommonUtils.showSnackBar(this, "Email Address is Mandatory", root)
            return false
        } else if (!isValidEmailId(tiEtEmail.text.toString().trim())) {
            CommonUtils.showSnackBar(this, "Invalid Email Address", root)
            return false
        } else if (etPhoneNo.isEnabled && etPhoneNo.text.toString().trim() == "") {
            CommonUtils.showSnackBar(this, "Mobile no is mandatory", root)
            return false
        } else if (etPhoneNo.text.toString().trim().length < 10) {
            CommonUtils.showSnackBar(this, "Mobile no must contain 10 digit", root)
            return false
        }
        return true
    }

}