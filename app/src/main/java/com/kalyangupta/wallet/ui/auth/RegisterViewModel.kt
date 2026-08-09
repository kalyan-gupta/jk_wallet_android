package com.kalyangupta.wallet.ui.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.repository.AuthRepository
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _registerEvent = MutableSharedFlow<RegisterEvent>()
    val registerEvent = _registerEvent.asSharedFlow()

    fun onUsernameChange(value: String) { _username.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun register() {
        if (_username.value.isBlank() || _password.value.isBlank()) return
        
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.register(_username.value, _email.value, _password.value)
            _isLoading.value = false
            when (result) {
                is Resource.Success -> _registerEvent.emit(RegisterEvent.Success)
                is Resource.Error -> _registerEvent.emit(RegisterEvent.Error(result.message ?: "Failed"))
                else -> {}
            }
        }
    }

    sealed class RegisterEvent {
        object Success : RegisterEvent()
        data class Error(val message: String) : RegisterEvent()
    }
}
