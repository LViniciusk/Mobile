package com.example.taskliteapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.taskliteapp.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks
    private var nextId = 1
    fun onAddTask (title: String) {
        if (title.isNotBlank()) {
            val newTask = Task(id = nextId++, title = title, isDone = false)
            _tasks.value += newTask
        }
    }
    fun onTaskChecked (id: Int) {
        _tasks.value = _tasks.value.map {
            if (it.id == id) it.copy(isDone = !it.isDone) else it
        }
    }
}