package com.example.guardianangel.ui.sos

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.guardianangel.databinding.FragmentSosBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SosFragment : Fragment() {

    private var _binding: FragmentSosBinding? = null
    private val binding get() = _binding!!

    private var isEmergencyActive = false
    private var countDownTimer: CountDownTimer? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSosBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSos.setOnClickListener {
            if (!isEmergencyActive) {
                initiateEmergency()
            } else {
                cancelEmergency()
            }
        }
    }

    private fun initiateEmergency() {
        isEmergencyActive = true
        binding.tvStatus.text = "Preparing Alert..."

        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = "Sending in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                binding.tvCountdown.text = "Alert Sent!"
                binding.tvStatus.text = "Help is on the way"
                getRealLocationAndSendSMS()
            }
        }.start()
    }

    private fun getRealLocationAndSendSMS() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                val lat = location?.latitude ?: 0.0
                val lng = location?.longitude ?: 0.0
                val mapUrl = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
                fetchContactsAndSend(mapUrl)
            }
        } else {
            fetchContactsAndSend("Location permission denied.")
        }
    }

    private fun fetchContactsAndSend(locationInfo: String) {
        // Fetches from "contacts" collection
        db.collection("contacts")
            .get()
            .addOnSuccessListener { documents ->
                val phoneNumbers = mutableListOf<String>()
                for (document in documents) {
                    val phone = document.getString("phone")
                    if (phone != null) phoneNumbers.add(phone)
                }

                if (phoneNumbers.isNotEmpty()) {
                    dispatchSMS(phoneNumbers, locationInfo)
                } else {
                    Toast.makeText(requireContext(), "No contacts found!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun dispatchSMS(numbers: List<String>, locationInfo: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = requireContext().getSystemService(SmsManager::class.java)
                val message = "EMERGENCY! My location: $locationInfo"
                for (number in numbers) {
                    smsManager.sendTextMessage(number, null, message, null, null)
                }
                Toast.makeText(requireContext(), "SOS Sent!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "SMS Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelEmergency() {
        isEmergencyActive = false
        countDownTimer?.cancel()
        binding.tvStatus.text = "Safe"
        binding.tvCountdown.text = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}