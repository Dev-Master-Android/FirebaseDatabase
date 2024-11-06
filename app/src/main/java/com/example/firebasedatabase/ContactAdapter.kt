package com.example.firebasedatabase

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasedatabase.databinding.ItemContactBinding
import com.example.firebasedatabase.model.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactAdapter : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    private val contacts = mutableListOf<Contact>()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userId: String = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int = contacts.size

    fun submitList(contactList: List<Contact>) {
        contacts.clear()
        contacts.addAll(contactList)
        notifyDataSetChanged()
    }

    inner class ContactViewHolder(private val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.nameTextView.text = contact.name
            binding.phoneTextView.text = contact.phone

            itemView.setOnClickListener {
                val contactToRemove = contacts[adapterPosition]
                removeContact(contactToRemove)
            }
        }

        private fun removeContact(contact: Contact) {
            firestore.collection("users").document(userId).collection("contacts")
                .whereEqualTo("name", contact.name)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                            .addOnSuccessListener {
                                Toast.makeText(binding.root.context, "Контакт удален", Toast.LENGTH_SHORT).show()
                                contacts.remove(contact)
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(binding.root.context, "Ошибка удаления: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(binding.root.context, "Ошибка загрузки данных: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
