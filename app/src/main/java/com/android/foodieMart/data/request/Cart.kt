package com.android.foodieMart.data.request

data class Cart(
    var cartItems: MutableList<CartItem> = mutableListOf()
)
