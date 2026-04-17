package com.example.guardianangel.ui.contacts

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guardianangel.R
import com.example.guardianangel.databinding.FragmentContactsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Contact(
    val id: String = "",
    val name: String = "",
    val phone: String = ""
)

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val contactsList = mutableListOf<Contact>()
    private lateinit var adapter: ContactsAdapter

    private val pickContact = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@registerForActivityResult
        val uri = data.data ?: return@registerForActivityResult

        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                val name = it.getString(nameIndex) ?: "Unknown"
                val contactId = it.getString(idIndex)

                val phoneCursor = requireContext().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(contactId),
                    null
                )
                phoneCursor?.use { pc ->
                    if (pc.moveToFirst()) {
                        val phoneIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val phone = pc.getString(phoneIndex) ?: ""
                        saveContactToFirestore(name, phone)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContactsAdapter(
            contactsList,
            onDelete = { contact -> deleteContact(contact) },
            onCall = { contact -> callContact(contact) }
        )

        binding.recyclerContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerContacts.adapter = adapter

        binding.fabAddContact.setOnClickListener {
            showAddContactDialog()
        }

        loadContacts()
    }

    private fun showAddContactDialog() {
        val options = arrayOf("Type manually", "Pick from phone contacts")
        AlertDialog.Builder(requireContext())
            .setTitle("Add Emergency Contact")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showManualInputDialog()
                    1 -> openPhoneContacts()
                }
            }
            .show()
    }

    private fun showManualInputDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val nameInput = EditText(requireContext()).apply {
            hint = "Contact Name"
        }
        val phoneInput = EditText(requireContext()).apply {
            hint = "Phone Number (e.g. +91xxxxxxxxxx)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        layout.addView(nameInput)
        layout.addView(phoneInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Contact")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    saveContactToFirestore(name, phone)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openPhoneContacts() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContact.launch(intent)
    }

    private fun saveContactToFirestore(name: String, phone: String) {
        val uid = auth.currentUser?.uid ?: "default"

        val contact = hashMapOf(
            "name" to name,
            "phone" to phone,
            "uid" to uid
        )

        db.collection("contacts")
            .add(contact)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "$name added!", Toast.LENGTH_SHORT).show()
                loadContacts()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add contact", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadContacts() {
        db.collection("contacts")
            .get()
            .addOnSuccessListener { documents ->
                contactsList.clear()
                for (doc in documents) {
                    val contact = Contact(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        phone = doc.getString("phone") ?: ""
                    )
                    contactsList.add(contact)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load contacts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteContact(contact: Contact) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Contact")
            .setMessage("Remove ${contact.name} from emergency contacts?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("contacts").document(contact.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "${contact.name} removed", Toast.LENGTH_SHORT).show()
                        loadContacts()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun callContact(contact: Contact) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ContactsAdapter(
    private val contacts: List<Contact>,
    private val onDelete: (Contact) -> Unit,
    private val onCall: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phone
        holder.tvAvatar.text = contact.name.firstOrNull()?.uppercase() ?: "?"
        holder.btnDelete.setOnClickListener { onDelete(contact) }
        holder.itemView.setOnClickListener { onCall(contact) }
    }

    override fun getItemCount() = contacts.size
}