package com.example.guardianangel.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.guardianangel.databinding.FragmentDashboardBinding
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadContactsCount()
        fetchLocation()

        binding.btnTestSos.setOnClickListener {
            Toast.makeText(requireContext(), "🚨 SOS Test triggered! Go to SOS tab to send.", Toast.LENGTH_LONG).show()
        }

        binding.btnShareLocation.setOnClickListener {
            shareLocation()
        }
    }

    private fun loadContactsCount() {
        db.collection("contacts")
            .get()
            .addOnSuccessListener { documents ->
                binding.tvContactsCount.text = documents.size().toString()
            }
    }

    private fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    binding.tvLocation.text = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                } else {
                    binding.tvLocation.text = "Location unavailable. Open Maps first."
                }
            }
        } else {
            binding.tvLocation.text = "Location permission not granted"
        }
    }

    private fun shareLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val locationUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "My current location: $locationUrl")
                    }
                    startActivity(Intent.createChooser(intent, "Share Location"))
                } else {
                    Toast.makeText(requireContext(), "Location unavailable. Open Maps first.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}