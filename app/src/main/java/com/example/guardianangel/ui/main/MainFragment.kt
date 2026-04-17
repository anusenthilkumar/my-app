package com.example.guardianangel.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.guardianangel.R
import com.example.guardianangel.databinding.FragmentMainBinding
import com.example.guardianangel.ui.dashboard.DashboardFragment
import com.example.guardianangel.ui.contacts.ContactsFragment
import com.example.guardianangel.ui.settings.SettingsFragment
import com.example.guardianangel.ui.sos.SosFragment

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_sos -> {
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.nav_dashboard -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.nav_contacts -> {
                    binding.viewPager.currentItem = 2
                    true
                }
                R.id.nav_settings -> {
                    binding.viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class MainPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SosFragment()
                1 -> DashboardFragment()
                2 -> ContactsFragment()
                3 -> SettingsFragment()
                else -> SosFragment()
            }
        }
    }
}