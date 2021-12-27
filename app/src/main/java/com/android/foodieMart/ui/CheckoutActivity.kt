package com.android.foodieMart.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.foodieMart.R
import com.android.foodieMart.data.request.*
import com.android.foodieMart.dialog.ViewDialog
import com.android.foodieMart.network.firebase.Connection
import com.android.foodieMart.network.firebase.FirebaseMethods
import com.android.foodieMart.network.firebase.RequestCallback
import com.android.foodieMart.notification.*
import com.android.foodieMart.ui.adapter.CheckoutAdapter
import com.android.foodieMart.utils.CommonUtils
import com.android.foodieMart.utils.Constants
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.android.synthetic.main.app_header.*
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*


class CheckoutActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var loadingDialog: ViewDialog
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var order: Order
    private lateinit var adapter: CheckoutAdapter
    private lateinit var apiService: NotificationService
    var simpleDateOnlyFormat: SimpleDateFormat? = null
    private var timingArray =
        arrayOf(
            "Select Delivery Time",
            "10.00 AM to 12.00 PM",
            "12.00 PM to 2.00 PM",
            "2.00 PM to 4.00 PM",
            "4.00 PM to 6.00 PM"
        )
    private var selectedTiming = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        simpleDateOnlyFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        apiService = Client.getClient()?.create(NotificationService::class.java)!!
        loadingDialog = ViewDialog(this)
        //extract order id from intent
        intent?.let {
            val orderStr = it.getStringExtra(Connection.ORDERS) as String
            order = Gson().fromJson(orderStr, Order::class.java)
        }
        spinner_time.onItemSelectedListener = this
        val aaWeight: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, timingArray)
        aaWeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_time.adapter = aaWeight

        setViews()
        setRecyclerView()
        loadCheckoutItems()
    }

    private fun setViews() {
        loadingDialog.showDialog()
        FirebaseMethods.singleValueEventChild(Connection.USERS, userId, object : RequestCallback {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                loadingDialog.hideDialog()
                dataSnapshot.getValue(String::class.java)?.let {
                    val user = Gson().fromJson(it, User::class.java)
                    user?.let {
                        tv_delivery_address.text = user.address
                    }
                }
            }
        })
        tvHeader.text = "Checkout"

        //radio buttons
        rb_pickup.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                addressSectionVisibility(View.GONE)
                rb_home_delivery.isChecked = false
            } else {
                addressSectionVisibility(View.VISIBLE)
                rb_home_delivery.isChecked = true
            }
        }
        rb_home_delivery.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                addressSectionVisibility(View.VISIBLE)
                rb_pickup.isChecked = false
            } else {
                addressSectionVisibility(View.GONE)
                rb_pickup.isChecked = true
            }
        }
        tilDate.setOnClickListener {
            SingleDateAndTimePickerDialog.Builder(this) //.bottomSheet()
                .bottomSheet()
                .curved()
                .displayMinutes(false)
                .displayHours(false)
                .displayDays(false)
                .displayMonth(true)
                .displayYears(true)
                .todayText("Today")
                .mustBeOnFuture()
                .displayDaysOfMonth(true)
                .displayListener(object : SingleDateAndTimePickerDialog.DisplayListener {
                    override fun onDisplayed(picker: SingleDateAndTimePicker) {
                        // Retrieve the SingleDateAndTimePicker
                    }

                    fun onClosed(picker: SingleDateAndTimePicker?) {
                        // On dialog closed
                    }
                })
                .title("Delivery Date")
                .listener {
                    tilDate.text = simpleDateOnlyFormat?.format(it)
                    order.delivery_date = simpleDateOnlyFormat?.format(it).toString()
                    //Toast.makeText(this,simpleDateOnlyFormat?.format(it),Toast.LENGTH_SHORT).show()
                }.display()
        }

        btn_proceed.setOnClickListener {
            if (tilDate.text.toString() == "") {
                CommonUtils.showSnackBar(this, "Please select Delivery Date", root)
            } else if (spinner_time.selectedItem.toString() == "Select Delivery Time") {
                CommonUtils.showSnackBar(this, "Please select Delivery Time", root)
            } else {
                loadingDialog.showDialog()
                FirebaseMethods.singleValueEvent(
                    Connection.SHOP_OPEN_STATUS,
                    object : RequestCallback {
                        override fun onDataChanged(dataSnapshot: DataSnapshot) {
                            dataSnapshot.getValue(Boolean::class.java)?.let {
                                if (it) {
                                    startOrder()
                                } else {
                                    loadingDialog.hideDialog()
                                    val mDialog = MaterialDialog.Builder(this@CheckoutActivity)
                                        .setMessage("Currently not taking any order")
                                        .setCancelable(false)
                                        .setPositiveButton(
                                            "Retry"
                                        ) { dialogInterface, which ->
                                            dialogInterface.dismiss()
                                        }
                                        .setNegativeButton("Cancel") { dialogInterface, which ->
                                            dialogInterface.dismiss()
                                            finish()
                                        }
                                        .build()
                                    mDialog.show()
                                }
                            }
                        }
                    })
            }
        }

        tv_change.setOnClickListener {
            loadingDialog.showDialog()
            FirebaseMethods.singleValueEventChild(
                Connection.USERS,
                userId,
                object : RequestCallback {
                    override fun onDataChanged(dataSnapshot: DataSnapshot) {
                        loadingDialog.hideDialog()
                        val userStr = dataSnapshot.getValue(String::class.java)
                        val intent = Intent(this@CheckoutActivity, AddressActivity::class.java)
                        intent.putExtra(Connection.USERS, userStr)
                        startActivityForResult(intent, 190)
                    }
                })
        }

        ivBackHeader.setOnClickListener { onBackPressed() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            190 -> {
                if (resultCode == RESULT_OK) {
                    data?.let {
                        val userStr = it.getStringExtra(Connection.USERS)
                        userStr?.let {
                            order.order_user = Gson().fromJson(it, User::class.java)
                            tv_delivery_address.text = order.order_user.address
                        }
                    }
                }
            }
        }
    }

    private fun startOrder() {
        FirebaseMethods.addValueEventChild(Connection.USERS, userId, object : RequestCallback {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                val userStr = dataSnapshot.getValue(String::class.java)
                val user = Gson().fromJson(userStr, User::class.java)
                order.order_user = user
                order.isPickUp = rb_pickup.isChecked
                val feeds = mutableListOf<String>()
                order.feeds.forEach {
                    feeds.add(it.feed.id)
                }
                FirebaseMethods.addValueEvent(Connection.FEED, object : RequestCallback {
                    override fun onDataChanged(dataSnapshot: DataSnapshot) {
                        var feedAdded = true
                        dataSnapshot.children.forEach {
                            val feedStr = it.getValue(String::class.java)
                            val feed = Gson().fromJson(feedStr, Feed::class.java)
                            if (feeds.contains(feed.id)) {
                                var pos = feeds.indexOf(feed.id)
                                if (feed.isAvailable) {
                                    if (order.feeds[pos].feedCount <= feed.total_stock_size) {
                                        order.feeds[pos].feed = feed
                                    } else {
                                        feedAdded = false
                                        cancelOrder(
                                            order, "Your Order has been placed cancelled. " +
                                                    "${order.feeds[pos].feed.name} is only ${order.feeds[pos].feed.total_stock_size} left in stock " +
                                                    " and your order count is ${order.feeds[pos].feedCount}. Sorry for the inconvenience."
                                        )
                                    }
                                } else {
                                    feedAdded = false
                                    cancelOrder(
                                        order,
                                        "${feed.name} is not avialable. Please try again later"
                                    )
                                }
                            }
                        }
                        if (feedAdded) {
                            confirmOrder()
                        }
                    }
                })
            }
        })
    }

    private fun cancelOrder(order: Order, message: String) {
        loadingDialog.hideDialog()
        val mDialog = MaterialDialog.Builder(this@CheckoutActivity)
            .setTitle("Order Cancelled")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(
                "Okay"
            ) { dialogInterface, which ->
                val intent =
                    Intent(this@CheckoutActivity, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                dialogInterface.dismiss()
            }
            .build()
        mDialog.show()
    }

    private fun setRecyclerView() {
        rv_cart.layoutManager = LinearLayoutManager(this)
        adapter = CheckoutAdapter(this)
        rv_cart.adapter = adapter
    }

    private fun loadCheckoutItems() {
        adapter.setList(order.feeds)
        loadAmounts(order.feeds)

    }

    private fun addInMyOrders(key: String) {
        FirebaseMethods.singleValueEventChild(
            Connection.MY_ORDERS,
            userId,
            object : RequestCallback {
                override fun onDataChanged(dataSnapshot: DataSnapshot) {
                    var myOrders = MyOrders()
                    if (dataSnapshot.exists()) {
                        val orderStr = dataSnapshot.getValue(String::class.java)
                        myOrders = Gson().fromJson(orderStr, MyOrders::class.java)
                    }
                    myOrders.order_list.add(key)
                    val updatedMyOrderStr = Gson().toJson(myOrders)
                    FirebaseDatabase.getInstance().reference.child(Connection.MY_ORDERS)
                        .child(userId).setValue(updatedMyOrderStr).addOnCompleteListener {
                            if (it.isSuccessful) {
                                loadingDialog.hideDialog()
                                val mDialog = MaterialDialog.Builder(this@CheckoutActivity)
                                    .setTitle("Order Placed")
                                    .setMessage("Your Order has been placed successfully.We will inform you when order is ready.")
                                    .setCancelable(false)
                                    .setPositiveButton(
                                        "Okay"
                                    ) { dialogInterface, which ->
                                        /*val intent =
                                            Intent(this@CheckoutActivity, HomeActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                        finish()*/
                                        dialogInterface.dismiss()
                                        sendNotification()
                                    }
                                    .build()
                                mDialog.show()
                            } else {
                                loadingDialog.hideDialog()
                                val mDialog = MaterialDialog.Builder(this@CheckoutActivity)
                                    .setMessage("Not able to place order.Please try again")
                                    .setCancelable(false)
                                    .setPositiveButton(
                                        "Retry"
                                    ) { dialogInterface, which ->
                                        dialogInterface.dismiss()
                                    }
                                    .setNegativeButton("Cancel") { dialogInterface, which ->
                                        dialogInterface.dismiss()
                                    }
                                    .build()
                                mDialog.show()
                            }
                        }
                }
            })
    }

    private fun confirmOrder() {
        FirebaseMethods.singleValueEvent(Connection.ORDER_COUNT_STATUS_ID,
            object : RequestCallback {
                override fun onDataChanged(dataSnapshot: DataSnapshot) {
                    dataSnapshot.getValue(Long::class.java)?.let {
                        order.order_id = it.toString()
                        FirebaseDatabase.getInstance().reference.child(Connection.ORDER_COUNT_STATUS_ID)
                            .setValue(it + 1).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val databaseReference =
                                        FirebaseDatabase.getInstance().reference.child(Connection.ORDERS)
                                    order.id = databaseReference.push().key!!
                                    order.status = "In Progress"
                                    if (tv_delivery_fee_value.text.equals("Free") == false) {
                                        order.deliveryFree =
                                            Integer.parseInt(tv_delivery_fee_value.text.toString().substring(1))
                                    }
                                    val orderStr = Gson().toJson(order)
                                    databaseReference.child(order.id).setValue(orderStr)
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                addInMyOrders(order.id)
                                                removeOrderFromCarts()
                                            } else {
                                                loadingDialog.hideDialog()
                                            }
                                        }
                                }
                            }
                    }
                }
            })
    }

    private fun removeOrderFromCarts() {
        FirebaseDatabase.getInstance().reference.child(Connection.CART).child(userId).removeValue()
    }

    private fun sendNotification() {
        FirebaseMethods.addValueEvent(Connection.SELLERS, object : RequestCallback {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                val sellers = mutableListOf<String>()
                dataSnapshot.children.forEach {
                    val sellerSTr = it.getValue(String::class.java)
                    val seller = Gson().fromJson(sellerSTr, Seller::class.java)
                    sellers.add(seller.id)
                }
                FirebaseMethods.addValueEvent(Connection.TOKENS, object : RequestCallback {
                    override fun onDataChanged(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach {
                            if (sellers.contains(it.key.toString())) {
                                val token = it.getValue(String::class.java)
                                sendNotification(token!!)
                            }
                        }
                        loadingDialog.hideDialog()
                        finish()
                    }
                })
            }
        })
    }

    private fun sendNotification(token: String) {
        val data = Data().apply {
            this.body = "Order placed by ${order.order_user.username}"
            this.orderId = order.id
            this.purpose = "Order Placed"
        }
        val sender = Sender().apply {
            this.data = data
            this.to = token
        }
        apiService.sendNotification(sender)
            .enqueue(object : retrofit2.Callback<Response> {
                override fun onFailure(call: Call<Response>, t: Throwable) {
                }

                override fun onResponse(
                    call: Call<Response>,
                    response: retrofit2.Response<Response>
                ) {
                }
            })
    }

    private fun loadAmounts(feeds: MutableList<OrderFeedItem>) {
        var itemTotal = 0
        feeds.forEach {
            val price = it.feed.feed_types[it.selected_feed_item].price
            itemTotal += (price * it.feedCount)
        }
        tv_item_total_value.text = Constants.rupee + "${itemTotal}"

        FirebaseMethods.addValueEvent(Connection.DELIVERY_RANGE,object : RequestCallback{
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                dataSnapshot?.let {
                    val range = dataSnapshot.getValue(String::class.java)
                    val price= Gson().fromJson<DeliveryPrice>(range,DeliveryPrice::class.java)
                    var deliveryFeeAmount=0
                    if (itemTotal<400){
                        if (price.price1==0){
                            tv_delivery_fee_value.text="Free"
                        }else{
                            deliveryFeeAmount=price.price1
                            tv_delivery_fee_value.text= Constants.rupee + price.price1.toString()
                        }
                    }else if (itemTotal>=400){
                        if (price.price2==0){
                            tv_delivery_fee_value.text="Free"
                        }else{
                            deliveryFeeAmount=price.price2
                            tv_delivery_fee_value.text= Constants.rupee + price.price2.toString()
                        }
                    }
                    val deliveryFee = if (tv_delivery_fee_value.text.equals("Free")) 0 else deliveryFeeAmount

                    val total = itemTotal + deliveryFee
                    tv_total_value.text = Constants.rupee + "${total}"
                }
            }
        })

    }

    private fun addressSectionVisibility(visibilty: Int) {
        tv_delivery_address_to.visibility = visibilty
        tv_delivery_address.visibility = visibilty
        tv_change.visibility = visibilty
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedTiming = timingArray[position]
        order.delivery_time = selectedTiming
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}