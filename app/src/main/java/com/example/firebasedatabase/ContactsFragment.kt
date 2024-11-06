package com.example.firebasedatabase

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasedatabase.databinding.FragmentContactsBinding
import com.example.firebasedatabase.model.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactsFragment : Fragment() {
    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        contactAdapter = ContactAdapter()

        // Настройка RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = contactAdapter

        // Загрузка контактов из базы данных
        loadContacts()

        // Сохранение нового контакта
        binding.saveButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val phone = binding.phoneEditText.text.toString()
            if (name.isNotEmpty() && phone.isNotEmpty()) {
                saveContact(Contact(name, phone))
                binding.nameEditText.text.clear()
                binding.phoneEditText.text.clear()
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadContacts() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).collection("contacts")
                .get()
                .addOnSuccessListener { documents ->
                    val contacts = mutableListOf<Contact>()
                    for (document in documents) {
                        val contact = document.toObject(Contact::class.java)
                        contacts.add(contact)
                    }
                    contactAdapter.submitList(contacts)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Ошибка загрузки данных: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ContactsFragment", "Ошибка загрузки данных", exception)
                }
        }
    }

    private fun saveContact(contact: Contact) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).collection("contacts")
                .add(contact)
                .addOnSuccessListener {
                    Log.d("ContactsFragment", "Контакт успешно сохранен: ${contact.name}")
                    Toast.makeText(requireContext(), "Контакт сохранен", Toast.LENGTH_SHORT).show()
                    loadContacts()
                }
                .addOnFailureListener { exception ->
                    Log.e("ContactsFragment", "Ошибка сохранения: ${exception.message}")
                    Toast.makeText(requireContext(), "Ошибка сохранения: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("ContactsFragment", "Пользователь не авторизован")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
