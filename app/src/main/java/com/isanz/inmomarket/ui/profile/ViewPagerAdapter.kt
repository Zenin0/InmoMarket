package com.isanz.inmomarket.ui.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.isanz.inmomarket.ui.profile.tabs.favorites.FavoritesProfileFragment
import com.isanz.inmomarket.ui.profile.tabs.uploads.YourUploadsProfileFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoritesProfileFragment()
            1 -> YourUploadsProfileFragment()
            else -> error("Invalid position")
        }
    }
}