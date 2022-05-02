package com.jojo.android.mwodeola.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuInflater
import android.widget.LinearLayout
import com.jojo.android.mwodeola.R

class MyBottomNavigationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {


    init {
        context.obtainStyledAttributes(attrs, R.styleable.MyBottomNavigationView, defStyleAttr, defStyleRes).let {


            it.recycle()
        }

        orientation = HORIZONTAL
    }
}