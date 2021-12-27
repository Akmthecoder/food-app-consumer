package com.android.foodieMart.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.foodieMart.R
import com.android.foodieMart.data.request.Cart
import com.android.foodieMart.data.request.CartItem
import com.android.foodieMart.data.request.Order
import com.android.foodieMart.data.request.OrderFeedItem
import com.android.foodieMart.network.firebase.Connection
import com.android.foodieMart.network.firebase.FirebaseMethods
import com.android.foodieMart.network.firebase.RequestCallback
import com.android.foodieMart.ui.adapter.MyCartAdapter
import com.android.foodieMart.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_cart.*
import kotlinx.android.synthetic.main.layout_button.*

class CartFragment : Fragment() {
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var adapter: MyCartAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setRecyclerView()
        loadCartItems()
    }

    private fun setViews() {

        //set Button
        tv_start?.setText("Checkout")
        tv_start?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cart, 0, 0, 0)
        tv_start?.compoundDrawablePadding = 16

        btn_proceed.setOnClickListener {
            val orderItems = mutableListOf<OrderFeedItem>()
            adapter.cartItems?.forEach {
                orderItems.add(OrderFeedItem(it.feed, it.feedCount, it.selected_feed_item))
            }
            val order = Order().apply {
                this.feeds = orderItems
            }
            val orderStr = Gson().toJson(order)
            val intent = Intent(context, CheckoutActivity::class.java)
            intent.putExtra(Connection.ORDERS, orderStr)
            startActivity(intent)
        }
    }

    private fun setRecyclerView() {
        context?.let {
            rv_cart?.layoutManager = LinearLayoutManager(it)
            adapter = MyCartAdapter(it, ::deleteCartItem, ::loadAmounts)
            rv_cart?.adapter = adapter
        }
    }

    private fun loadCartItems() {
        FirebaseMethods.addValueEventChild(Connection.CART, userId, object : RequestCallback {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val cartStr = dataSnapshot.getValue(String::class.java)
                    val cart = Gson().fromJson(cartStr, Cart::class.java)
                    adapter.setList(cart.cartItems)
                    cartVisibility(cart.cartItems.size == 0)
                    loadCartAmountTotal(cart.cartItems)
                } else {
                    cartVisibility(true)
                }
            }
        })
    }

    private fun loadCartAmountTotal(cartItems: MutableList<CartItem>) {
        var itemTotal = 0
        cartItems.forEach {
            val price = it.feed.feed_types[it.selected_feed_item].price
            itemTotal += (price * it.feedCount)
        }

        updateAmount(itemTotal)
    }

    private fun updateAmount(itemTotal: Int) {
        tv_total_value?.setText(Constants.rupee + "${itemTotal}")
        tv_value?.setText(Constants.rupee + "${itemTotal}")
    }


    private fun deleteCartItem(cartItems: MutableList<CartItem>, position: Int) {
        cartItems.removeAt(position)
        val cart = Cart(cartItems)
        val updatedCartStr = Gson().toJson(cart)
        FirebaseDatabase.getInstance().reference.child(Connection.CART)
            .child(userId)
            .setValue(updatedCartStr)
    }

    private fun loadAmounts(increaseAmount: Boolean, amount: Int) {
        var itemTotalCurrent = tv_value.text.toString()
        val updatedTotal = itemTotalCurrent.substring(1)
        var itemTotal = Integer.parseInt(updatedTotal)
        if (increaseAmount) {
            itemTotal += amount
        } else {
            itemTotal -= amount
        }
        tv_total_value?.setText(Constants.rupee + "${itemTotal}")
        tv_value?.setText(Constants.rupee + "${itemTotal}")
    }

    private fun cartVisibility(feedsEmpty: Boolean) {
        if (feedsEmpty) {
            cl_root?.visibility = View.GONE
            cl_empty_cart?.visibility = View.VISIBLE
        } else {
            cl_empty_cart?.visibility = View.GONE
            cl_root?.visibility = View.VISIBLE
        }
    }
}