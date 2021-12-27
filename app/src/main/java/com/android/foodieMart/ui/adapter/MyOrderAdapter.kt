package com.android.foodieMart.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.android.foodieMart.R
import com.android.foodieMart.data.request.Order
import com.android.foodieMart.utils.Constants
import java.util.*

class MyOrderAdapter(private val context: Context) :
    RecyclerView.Adapter<MyOrderAdapter.Viewholder>() {
    private var orderList: MutableList<Order> = mutableListOf()

    class Viewholder(item: View) : RecyclerView.ViewHolder(item) {
        var orderId : AppCompatTextView = item.findViewById(R.id.tv_order_id)
        var status: AppCompatTextView = item.findViewById(R.id.tv_order_status)
        var orderPrize: AppCompatTextView = item.findViewById(R.id.tv_order_prize)
        var orders: AppCompatTextView = item.findViewById(R.id.orders)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_my_order_item, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val order = orderList[position]
        var orderListStr = ""
        var price = 0
        order.feeds.forEach {
            var amount = "x${it.feedCount} ${it.feed.name}, "
            val feedPrice = it.feed.feed_types[it.selected_feed_item].price
            price += (feedPrice * it.feedCount)
            orderListStr += amount
        }
        if (order.deliveryFree.equals("Free") == false){
            price+= order.deliveryFree
        }
        val updatedOrderList = orderListStr.trim().substring(0, orderListStr.length - 1)
        holder.orders.setText(updatedOrderList)
        holder.orderPrize.setText("${Constants.rupee} ${price}")
        holder.orderId.setText("Order Id : ${order.order_id}")
        if (order.status.equals("Delivered") == false) {
            holder.status.setTextColor(R.color.colorPrimary)
        }
        holder.status.setText(order.status)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun setOrderList(orderList: MutableList<Order>) {
        Collections.reverse(orderList)
        this.orderList.clear()
        this.orderList.addAll(orderList)
        notifyDataSetChanged()
    }
}