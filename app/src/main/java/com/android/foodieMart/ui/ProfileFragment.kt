package com.android.foodieMart.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.android.foodieMart.R
import com.android.foodieMart.data.request.User
import com.android.foodieMart.network.firebase.Connection
import com.android.foodieMart.network.firebase.FirebaseMethods
import com.android.foodieMart.network.firebase.RequestCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.gson.Gson
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment() {
    private var user: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserData()
        if (FirebaseAuth.getInstance().currentUser?.phoneNumber != null) {
            iv_edit_phone.visibility = View.GONE
        }
        edit_name.setOnClickListener {
            tv_name.isCursorVisible = true
            tv_name.isEnabled = true
            tv_name.isFocusable = true  
            tv_name.performClick()
            tv_name.requestFocus()
            val imm: InputMethodManager? =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(tv_name, InputMethodManager.SHOW_IMPLICIT)
        }

        iv_edit_address.setOnClickListener {
            if (user.isEmpty()) {
                getUserData()
            } else {
                val intent = Intent(context, AddressActivity::class.java)
                intent.putExtra(Connection.USERS, user)
                startActivityForResult(intent, 800)
            }
        }

        iv_edit_phone.setOnClickListener {
            tv_phoneno.isCursorVisible = true
            tv_phoneno.isFocusable = true
            tv_phoneno.isEnabled = true
            tv_phoneno.performClick()
            tv_phoneno.requestFocus()
            val imm: InputMethodManager? =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(tv_name, InputMethodManager.SHOW_IMPLICIT)
        }
        tv_logout.setOnClickListener {
            val mDialog = MaterialDialog.Builder(requireActivity())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, which ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    dialogInterface.dismiss()
                    requireActivity().finish()
                }
                .setNegativeButton("No") { dialogInterface, which ->
                    dialogInterface.dismiss()
                }
                .build()
            mDialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 800) {
            data?.let {
                getUserData()
            }
        }
    }

    private fun getUserData() {
        FirebaseMethods.singleValueEventChild(
            Connection.USERS,
            FirebaseAuth.getInstance().currentUser?.uid.toString(),
            object : RequestCallback {
                override fun onDataChanged(dataSnapshot: DataSnapshot) {
                    val userStr = dataSnapshot.getValue(String::class.java)
                    userStr?.let {
                        this@ProfileFragment.user = it
                    }
                    val user = Gson().fromJson(userStr, User::class.java)
                    tv_name.setText(user.username)
                    tv_phoneno.setText(user.phone_number)
                    tv_email.text = user.email
                    if (user.address.isEmpty()) {
                        tv_address.setHint("Add Address")
                    } else {
                        tv_address.setText(user.address)
                    }
                }
            })
    }
}