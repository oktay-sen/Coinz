package com.oktaysen.coinz

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.oktaysen.coinz.backend.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var tabSelected:Int = R.id.navigation_map

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        tabSelected = item.itemId
        when (item.itemId) {
            R.id.navigation_inventory -> {
                message.setText(R.string.title_inventory)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_trading -> {
                message.setText(R.string.title_trading)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_map -> {
                message.setText(R.string.title_map)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_shop -> {
                message.setText(R.string.title_shop)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_leaderboard -> {
                message.setText(R.string.title_leaderboard)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = tabSelected
    }

    override fun onStart() {
        super.onStart()

        if (!Auth().isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        //TODO: Consider changing this behavior.
        if (tabSelected != R.id.navigation_map) {
            navigation.selectedItemId = R.id.navigation_map
        } else {
            super.onBackPressed()
        }
    }
}
