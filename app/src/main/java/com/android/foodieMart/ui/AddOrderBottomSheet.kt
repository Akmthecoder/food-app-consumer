package com.android.foodieMart.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.android.foodieMart.R
import com.android.foodieMart.data.request.Cart
import com.android.foodieMart.data.request.CartItem
import com.android.foodieMart.data.request.Feed
import com.android.foodieMart.dialog.ViewDialog
import com.android.foodieMart.network.firebase.Connection
import com.android.foodieMart.network.firebase.FirebaseMethods
import com.android.foodieMart.network.firebase.RequestCallback
import com.android.foodieMart.utils.CheckForLogin
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_button.*
import kotlinx.android.synthetic.main.layout_order_bottom_sheet.*

class AddOrderBottomSheet(var activity: Activity, private val feed: Feed) :
    BottomSheetDialogFragment(),
    View.OnClickListener {
    private var quantity: Int = 1
    private lateinit var loadingDialog: ViewDialog
    private var price: Int = 0
    private var selectedPosition = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_order_bottom_sheet, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = ViewDialog(requireContext())
        setBottomSheetView()
        onClick()
    }


    private fun onClick() {
        iv_minus.setOnClickListener(this)
        iv_plus.setOnClickListener(this)
        btn_proceed.setOnClickListener(this)
    }

    private fun setBottomSheetView() {
        if (feed.imageUrl.isNotEmpty()) {
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(16))
            Glide.with(this)
                .load(feed.imageUrl).apply(requestOptions).into(iv_feed)
        }
        price = feed.feed_types[0].price
        tv_value.text = "₹ ${price}"
        tv_feed_name.text = feed.name
        tv_feed_price.text = "₹ ${price}"
        tv_feed_description.text = feed.description

        val weights = arrayListOf<String>()
        feed.feed_types.forEach {
            weights.add(it.weight)
        }
        spinner_weight.adapter = ArrayAdapter(
            activity,
            R.layout.support_simple_spinner_dropdown_item,
            weights.toTypedArray()
        )
        spinner_weight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedPosition = p2
                price = feed.feed_types[selectedPosition].price
                tv_value.text = "₹ ${price*quantity}"
                tv_feed_price.text = "₹ ${price}"
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.iv_minus -> {
                if (quantity > 1) {
                    tv_quantity_amount.setText("${--quantity}")
                    val totalPrice =
                        quantity * price
                    tv_value.setText("₹ ${totalPrice}")
                }
            }
            R.id.iv_plus -> {
                if (quantity < feed.maxItems) {
                    tv_quantity_amount.setText("${++quantity}")
                    val totalPrice = quantity * price
                    tv_value.setText("₹ ${totalPrice}")
                }
            }
            R.id.btn_proceed -> {
                if (CheckForLogin.checkUserLoginOrNot(activity)) {
                    addCartItem(selectedPosition)
                }
            }
        }
    }

    private fun addCartItem(selectedPosition: Int) {
        loadingDialog.showDialog()
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        FirebaseMethods.singleValueEventChild(Connection.CART, userId, object : RequestCallback {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                lateinit var cart: Cart
                if (dataSnapshot.exists()) {
                    val cartStr = dataSnapshot.getValue(String::class.java)
                    cart = Gson().fromJson(cartStr, Cart::class.java)
                } else {
                    cart = Cart()
                }
                var position = -1
                var currentPos = 0
                cart.cartItems.forEach {
                    if (it.feed.id.equals(feed.id) && it.selected_feed_item == selectedPosition) {
                        position = currentPos
                    }
                    currentPos += 1
                }
                if (position >= 0) {
                    cart.cartItems.set(position, CartItem(feed, quantity, selectedPosition))
                } else {
                    cart.cartItems.add(CartItem(feed, quantity, selectedPosition))
                }
                val cartStr = Gson().toJson(cart)
                FirebaseDatabase.getInstance().reference
                    .child(Connection.CART)
                    .child(userId)
                    .setValue(cartStr)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            loadingDialog.hideDialog()
                            Toast.makeText(
                                requireContext(),
                                "Item Added to Cart Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                        } else {
                            loadingDialog.hideDialog()
                            Toast.makeText(
                                requireContext(),
                                "Something went wrong.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        })
    }
}