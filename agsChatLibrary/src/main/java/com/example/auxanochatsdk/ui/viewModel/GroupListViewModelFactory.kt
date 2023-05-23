package com.example.auxanochatsdk.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.auxanochatsdk.Repository.GroupRepository

class GroupListViewModelFactory(private val repository: GroupRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return GroupListViewModel(repository) as T
    }
}