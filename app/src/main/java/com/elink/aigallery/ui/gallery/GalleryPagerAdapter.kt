package com.elink.aigallery.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.elink.aigallery.fragments.GalleryTabFragment

class GalleryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return GalleryTabFragment.newInstance(
            if (position == 0) GalleryTabFragment.TYPE_FOLDERS else GalleryTabFragment.TYPE_CATEGORIES
        )
    }
}
