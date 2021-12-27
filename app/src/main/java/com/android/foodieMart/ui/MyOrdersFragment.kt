package com.android.foodieMart.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.foodieMart.R
import com.android.foodieMart.data.request.MyOrders
import com.android.foodieMart.data.request.Order
import com.android.foodieMart.network.firebase.Connection
import com.android.foodieMart.network.firebase.FirebaseMethods
import com.android.foodieMart.network.firebase.RequestCallback
import com.android.foodieMart.ui.adapter.MyOrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_my_orders.*

class MyOrdersFragment : Fragment() {
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var adapter: MyOrderAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        loadMyOrders()
    }

    private fun loadMyOrders() {
        FirebaseMethods.addValueEventChild(Connection.MY_ORDERS, userId, object : RequestCallback {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val myOrderStr = dataSnapshot.getValue(String::class.java)
                    val myOrders = Gson().fromJson(myOrderStr, MyOrders::class.java)
                    FirebaseMethods.addValueEvent(Connection.ORDERS, object : RequestCallback {
                        override fun onDataChanged(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val orderList = mutableListOf<Order>()
                                dataSnapshot.children.forEach {
                                    val orderStr = it.getValue(String::class.java)
                                    val order = Gson().fromJson(orderStr, Order::class.java)
                                    if (myOrders.order_list.contains(order.id)) {
                                        orderList.add(order)
                                    }
                                }
                                if (orderList.size == 0) {
                                    tv_empty_my_order?.visibility = View.VISIBLE
                                    rv_my_orders?.visibility = View.GONE
                                } else {
                                    tv_empty_my_order?.visibility = View.GONE
                                    rv_my_orders?.visibility = View.VISIBLE
                                    adapter.setOrderList(orderList)
                                }
                            }
                        }
                    })
                }
            }
        })
    }

    private fun setRecyclerView() {
        context?.let {
            rv_my_orders.layoutManager = LinearLayoutManager(it)
            adapter = MyOrderAdapter(it)
            rv_my_orders.adapter = adapter
        }
    }
}