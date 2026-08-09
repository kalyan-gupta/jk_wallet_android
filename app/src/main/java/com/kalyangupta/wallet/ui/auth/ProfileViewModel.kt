package com.kalyangupta.wallet.ui.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.UserDto
import com.kalyangupta.wallet.data.repository.AuthRepository
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _isStaff = mutableStateOf(false)
    val isStaff: State<Boolean> = _isStaff

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentUserId: Int = -1

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.getMe()
            _isLoading.value = false
            if (result is Resource.Success) {
                result.data?.let { user ->
                    currentUserId = user.id
                    _username.value = user.username
                    _email.value = user.email
                    _isStaff.value = user.isStaff
                }
            } else if (result is Resource.Error) {
                _error.value = result.message ?: "Failed to load profile"
                _eventFlow.emit(ProfileEvent.Error(result.message ?: "Failed to load profile"))
            }
        }
    }

    fun onUsernameChange(value: String) { _username.value = value }
    fun onEmailChange(value: String) { _email.value = value }

    fun updateProfile() {
        if (_username.value.isBlank()) return

        _isLoading.value = true
        viewModelScope.launch {
            val userDto = UserDto(
                id = currentUserId,
                username = _username.value,
                email = _email.value,
                isStaff = _isStaff.value
            )
            val result = authRepository.updateMe(userDto)
            _isLoading.value = false
            if (result is Resource.Success) {
                _eventFlow.emit(ProfileEvent.Success)
            } else if (result is Resource.Error) {
                _eventFlow.emit(ProfileEvent.Error(result.message ?: "Update failed"))
            }
        }
    }

    sealed class ProfileEvent {
        object Success : ProfileEvent()
        data class Error(val message: String) : ProfileEvent()
    }
}
