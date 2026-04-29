package com.example.guardianangel.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.guardianangel.R
import com.example.guardianangel.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("guardian_prefs", 0)
        binding.switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        binding.switchAutoSos.isChecked = prefs.getBoolean("auto_sos", false)
        binding.switchCountdown.isChecked = prefs.getBoolean("countdown", true)

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
        }

        binding.tvProfile.setOnClickListener {
            val email = auth.currentUser?.email ?: "Not logged in"
            AlertDialog.Builder(requireContext())
                .setTitle("My Profile")
                .setMessage("Email: $email")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.tvLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    logoutUser()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun logoutUser() {
        auth.signOut()

        // We use the Action ID we defined in the nav_graph.xml
        // This clears the backstack so the user cannot click "back" to return to the app
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()

        try {
            // Using the explicit action we added to the XML
            findNavController().navigate(R.id.action_mainFragment_to_loginFragment, null, navOptions)
        } catch (e: Exception) {
            // Fallback: Navigate to the fragment ID directly if the action fails
            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}c