package com.example.auxanochatsdk.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auxanochatsdk.Repository.GroupRepository
import com.example.auxanochatsdk.model.PreviousListModel
import com.example.auxanochatsdk.model.PreviousListModelItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatListViewModel(private val repository: GroupRepository) :
    ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.receiveChatListResponse()
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.receiveChatResponse()
        }
    }

    val chatLiveData: LiveData<PreviousListModel>
        get() = repository.chatLiveData
    val receiveMessage:LiveData<PreviousListModelItem>
    get() = repository.receiveLiveData
}