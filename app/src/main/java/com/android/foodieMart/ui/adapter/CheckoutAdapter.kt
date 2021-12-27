package com.android.foodieMart.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.android.foodieMart.R
import com.android.foodieMart.data.request.OrderFeedItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class CheckoutAdapter(private val context: Context) :
    RecyclerView.Adapter<CheckoutAdapter.Viewholder>() {
    val orderItems: MutableList<OrderFeedItem> = mutableListOf()

    class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var feedImage = itemView.findViewById<AppCompatImageView>(R.id.iv_feed)
        val feedName = itemView.findViewById<AppCompatTextView>(R.id.tv_feed_name)
        val feedPrize = itemView.findViewById<AppCompatTextView>(R.id.tv_feed_prize)
        val itemAmount = itemView.findViewById<AppCompatTextView>(R.id.tv_quantity_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_checkout_item, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val orderFeedItem = orderItems[position]
        val feed = orderFeedItem.feed
        if (feed.imageUrl.isNotEmpty()) {
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(16))
            Glide.with(context).load(feed.imageUrl).apply(requestOptions).into(holder.feedImage)
        }
        holder.feedName.setText(feed.name)
        holder.feedPrize.setText("₹ ${feed.feed_types[orderFeedItem.selected_feed_item].price}")
        holder.itemAmount.setText("x${orderFeedItem.feedCount}")
    }

    override fun getItemCount(): Int {
        return orderItems.size
    }

    fun setList(orderItems: MutableList<OrderFeedItem>) {
        this.orderItems.clear()
        this.orderItems.addAll(orderItems)
        notifyDataSetChanged()
    }
}