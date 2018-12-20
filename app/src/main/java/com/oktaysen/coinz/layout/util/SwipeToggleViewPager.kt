package com.oktaysen.coinz.layout.util

import android.content.Context
import android.view.MotionEvent
import android.support.v4.view.ViewPager
import android.util.AttributeSet

// Solution from https://stackoverflow.com/a/13437997
// Needed a way to disable navigation by swiping left & right, because we want to be able to swipe left & right on Mapbox instead.
class SwipeToggleViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    var swipingEnabled: Boolean = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.swipingEnabled) super.onTouchEvent(event) else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.swipingEnabled) super.onInterceptTouchEvent(event) else false
    }
}