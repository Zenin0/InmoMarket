import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.isanz.inmomarket.ui.profile.tabs.FavoritesProfileFragment
import com.isanz.inmomarket.ui.profile.tabs.YourUploadsProfileFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        // Return the total number of tabs
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        // Return a new instance of the Fragment for the given position
        return when (position) {
            0 -> FavoritesProfileFragment() // Replace with your actual Fragment class
            1 -> YourUploadsProfileFragment() // Replace with your actual Fragment class
            else -> throw IllegalStateException("Invalid position")
        }
    }
}