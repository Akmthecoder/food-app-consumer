package com.android.foodieMart.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.android.foodieMart.R
import com.android.foodieMart.data.request.CartItem
import com.bumptech.glide.Glide

class MyCartAdapter(
    private val context: Context,
    var deleteCartItem: (MutableList<CartItem>, Int) -> Unit,
    var changeAmounts: (Boolean, Int) -> Unit
) :
    RecyclerView.Adapter<MyCartAdapter.Viewholder>() {
    val cartItems: MutableList<CartItem> = mutableListOf()

    class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var feedImage = itemView.findViewById<AppCompatImageView>(R.id.iv_feed)
        val feedName = itemView.findViewById<AppCompatTextView>(R.id.tv_feed_name)
        val feedPrize = itemView.findViewById<AppCompatTextView>(R.id.tv_feed_prize)
        val itemAdd = itemView.findViewById<AppCompatImageView>(R.id.iv_plus)
        val itemRemove = itemView.findViewById<AppCompatImageView>(R.id.iv_minus)
        val itemAmount = itemView.findViewById<AppCompatTextView>(R.id.tv_quantity_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_cart_item, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val cartItem = cartItems[position]
        val feed = cartItem.feed
        val price = feed.feed_types[cartItem.selected_feed_item].price
        if (feed.imageUrl.isNotEmpty()) {
            Glide.with(context).load(feed.imageUrl).into(holder.feedImage)
        }

        holder.feedName.setText(feed.name)
        holder.feedPrize.setText("₹ ${price}")
        holder.itemAmount.setText("${cartItems[position].feedCount}")
        holder.itemAdd.setOnClickListener {
            if (cartItems[position].feedCount < feed.maxItems) {
                holder.itemAmount.setText("${++(cartItems[position].feedCount)}")
                changeAmounts(true, price)
            }
        }
        holder.itemRemove.setOnClickListener {
            if (cartItems[position].feedCount > 0) {
                holder.itemAmount.setText("${--(cartItems[position].feedCount)}")
                changeAmounts(false, price)
            }
            if (cartItems[position].feedCount == 0) {
                deleteCartItem(cartItems, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    fun setList(cartItems: MutableList<CartItem>) {
        this.cartItems.clear()
        this.cartItems.addAll(cartItems)
        notifyDataSetChanged()
    }
}