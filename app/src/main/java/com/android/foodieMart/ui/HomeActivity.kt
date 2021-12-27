package com.android.foodieMart.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.foodieMart.R
import com.android.foodieMart.data.request.Feed
import com.android.foodieMart.utils.CheckForLogin
import com.android.foodieMart.utils.Constants
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_header.*

class HomeActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment(::openBottomSheet)
    private val cartFragment = CartFragment()
    private val myOrdersFragment = MyOrdersFragment()
    private val profileFragment = ProfileFragment()
    private var openFragment = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        getIntentData()
        initializeViews()
    }

    private fun getIntentData() {
        intent?.let {
            if (it.hasExtra(Constants.PURPOSE)) {
                val purpose = it.getStringExtra(Constants.PURPOSE)
                purpose?.let {
                    if (purpose.equals(Constants.MYORDERS)) {
                        openFragment = 2
                        tvHeader.text = "My Orders"
                        if(supportFragmentManager.backStackEntryCount == 0) {
                            supportFragmentManager.beginTransaction()
                                .add(R.id.container, myOrdersFragment).commit()
                        }else{
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.container, myOrdersFragment).commit()
                        }
                    }
                }
            }
        }
    }

    private fun initializeViews() {
        tvHeader.text = "Home"
        ivBackHeader.visibility = View.GONE

        if (openFragment == 0) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, homeFragment).commit()
        }

        bottom_bar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    tvHeader.text = "Home"
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, homeFragment).commit()
                }
                R.id.myOrders -> {
                    if (CheckForLogin.checkUserLoginOrNot(this)) {
                        tvHeader.text = "My Orders"
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, myOrdersFragment).commit()
                    }
                }
                R.id.cart -> {
                    if (CheckForLogin.checkUserLoginOrNot(this)) {
                        tvHeader.text = "Cart"
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, cartFragment).commit()
                    }
                }
                R.id.profile -> {
                    if (CheckForLogin.checkUserLoginOrNot(this)) {
                        tvHeader.text = "Profile"
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, profileFragment).commit()
                    }
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun openBottomSheet(feed: Feed) {
        val bottomSheet = AddOrderBottomSheet(this, feed)
        bottomSheet.show(supportFragmentManager, "Order Sheet")
    }
}