package com.example.auxanochatsdk.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.auxanochatsdk.Repository.GroupRepository


class ChatListViewModelFactory(private val repository: GroupRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return ChatListViewModel(repository) as T
    }
}