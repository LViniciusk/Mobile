package com.example.cruditemapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.cruditemapp.model.Item
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ItemViewModel : ViewModel() {
    private val db = Firebase.firestore.collection("items")

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items = _items.asStateFlow()

    val titleState = mutableStateOf("")
    val descriptionState = mutableStateOf("")

    init {
        db.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _items.value = snapshot.toObjects()
            }
        }
    }

    fun addItem() {
        val newItem = Item(
            title = titleState.value,
            description = descriptionState.value
        )
        db.add(newItem)
        titleState.value = ""
        descriptionState.value = ""
    }

    fun updateItem(item: Item) {
        db.document(item.id).set(item)
    }

    fun deleteItem(itemId: String) {
        db.document(itemId).delete()
    }
}