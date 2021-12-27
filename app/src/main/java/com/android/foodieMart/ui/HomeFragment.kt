package com.android.foodieMart.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.foodieMart.R
import com.android.foodieMart.data.request.Feed
import com.android.foodieMart.network.firebase.Connection
import com.android.foodieMart.network.firebase.FirebaseMethods
import com.android.foodieMart.network.firebase.RequestCallback
import com.android.foodieMart.ui.adapter.FeedAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment(var openBottomSheet: (Feed) -> Unit) : Fragment() {
    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        setUpRecyclerView()
        loadFeed()
    }

    private fun initializeViews() {
        tbl_home?.let {
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        val category = it.text.toString()
                        adapter.setFeedCategory(category)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }
    }

    private fun setUpRecyclerView() {
        context?.let {
            val layoutManager = LinearLayoutManager(it)
            rv_feed.layoutManager = layoutManager
            adapter = FeedAdapter(it, ::openBottomSheetUtil)
            rv_feed.adapter = adapter
        }
    }

    private fun loadFeed() {
        progressBar.visibility = View.VISIBLE
        FirebaseMethods.addValueEvent(Connection.FEED, object : RequestCallback {
            override fun onDataChanged(dataSnapshot: DataSnapshot) {
                val feeds = mutableListOf<Feed>()
                val category = mutableListOf<String>()
                dataSnapshot.children.forEach {
                    val feedStr = it.getValue(String::class.java)
                    val feed = Gson().fromJson(feedStr, Feed::class.java)
                    if (!category.contains(feed.category) && feed.isAvailable) {
                        category.add(feed.category)
                    }
                    feed?.let {
                        if (feed.isAvailable) {
                            feeds.add(feed)
                        }
                    }
                }
                if (category.isNotEmpty()) {
                    addCategoryTabs(category)
                }
                tbl_home?.let {
                    val category = it.getTabAt(it.selectedTabPosition)?.text.toString()
                    if (feeds.size == 0) {
                        tv_empty_feed.visibility = View.VISIBLE
                        tbl_home.visibility = View.GONE
                        rv_feed.visibility = View.GONE
                    } else {
                        tv_empty_feed.visibility = View.GONE
                        tbl_home.visibility = View.VISIBLE
                        rv_feed.visibility = View.VISIBLE
                        adapter.setList(feeds, category)
                    }
                    progressBar.visibility = View.GONE
                }
            }
        })
    }

    private fun addCategoryTabs(category: List<String>) {
        tbl_home?.let {
            it.removeAllTabs()
            category.forEach { it1 ->
                val tab = it.newTab().setText(it1)
                it.addTab(tab)
            }
        }
    }

    private fun openBottomSheetUtil(feed: Feed) = openBottomSheet(feed)
}