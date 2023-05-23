package com.example.auxanochatsdk.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auxanochatsdk.Repository.GroupRepository
import com.example.soketdemo.Model.GroupListModelItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GroupListViewModel(private val repository: GroupRepository) :
    ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.receiveGroupListResponce()
        }
    }

    val groupLiveData: LiveData<List<GroupListModelItem>>
        get() = repository.groupLiveData
}