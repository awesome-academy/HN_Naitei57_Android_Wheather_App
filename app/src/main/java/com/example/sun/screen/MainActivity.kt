package com.example.sun.screen

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.example.sun.screen.home.HomeFragment
import com.example.sun.utils.base.BaseActivity
import com.example.weather.R

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
    }

    override fun inflateBinding(layoutInflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setNextFragment(HomeFragment.newInstance())
        setNavigation()
    }

    private fun setNavigation() {
        viewBinding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.mi_home -> setNextFragment(HomeFragment.newInstance())
            }
            true
        }
    }

    private fun setNextFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(fragment::javaClass.name)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
