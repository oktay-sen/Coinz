package com.oktaysen.coinz.layout.main

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.*
import com.oktaysen.coinz.layout.login.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.text.FieldPosition

class MainActivity : AppCompatActivity() {
    var currentTab = R.id.navigation_map
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val tabId = item.itemId
        val position = when (tabId) {
            R.id.navigation_inventory -> 0
            R.id.navigation_trading -> 1
            R.id.navigation_map -> 2
            R.id.navigation_shop -> 3
            R.id.navigation_leaderboard -> 4
            else -> return@OnNavigationItemSelectedListener false
        }
        tab_pager.setCurrentItem(position, true)
        return@OnNavigationItemSelectedListener true
    }

    private fun navigateTo(tabId: Int) {
        navigation.selectedItemId = tabId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tab_pager.adapter = TabPagerAdapter(supportFragmentManager)
        tab_pager.swipingEnabled = false
        tab_pager.offscreenPageLimit = 5
        tab_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
            override fun onPageSelected(position: Int) {
                val tabId = when (position) {
                    0 -> R.id.navigation_inventory
                    1 -> R.id.navigation_trading
                    2 -> R.id.navigation_map
                    3 -> R.id.navigation_shop
                    4 -> R.id.navigation_leaderboard
                    else -> {
                        Timber.e("Expected 0 <= position <= 4, got $position.")
                        return
                    }
                }
                navigateTo(tabId)
            }
        })

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigateTo(currentTab)
    }

    override fun onStart() {
        super.onStart()
        Auth().addAuthStateListener(::onAuthStateChange)
    }

    override fun onStop() {
        super.onStop()
        Auth().removeAuthStateListener(::onAuthStateChange)
    }

    private fun onAuthStateChange(isLoggedIn: Boolean) {
        if (!isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        //TODO: Consider changing this behavior.
        if (currentTab != R.id.navigation_map) {
            navigateTo(R.id.navigation_map)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.settings -> {
                return false
            }
            R.id.logout -> {
                Auth().logOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class TabPagerAdapter(fm: FragmentManager):FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> InventoryFragment()
                1 -> InventoryFragment() //TODO: Replace with unique fragment
                2 -> MapFragment()
                3 -> InventoryFragment() //TODO: Replace with unique fragment
                4 -> InventoryFragment() //TODO: Replace with unique fragment
                else -> throw Error("Expected 0 <= position <= 4, got $position.")
            }
        }

        override fun getCount(): Int = 5

    }
}
