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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isRegistrationEnabled = mutableStateOf(true)
    val isRegistrationEnabled: State<Boolean> = _isRegistrationEnabled

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    init {
        checkRegistrationStatus()
    }

    private fun checkRegistrationStatus() {
        viewModelScope.launch {
            val result = authRepository.getRegistrationStatus()
            if (result is Resource.Success) {
                _isRegistrationEnabled.value = result.data ?: true
            }
        }
    }

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun login() {
        if (_username.value.isBlank() || _password.value.isBlank()) {
            viewModelScope.launch {
                _loginEvent.emit(LoginEvent.Error("Username and password cannot be empty"))
            }
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.login(_username.value, _password.value)
            _isLoading.value = false
            when (result) {
                is Resource.Success -> {
                    _loginEvent.emit(LoginEvent.Success)
                }
                is Resource.Error -> {
                    _loginEvent.emit(LoginEvent.Error(result.message ?: "Unknown error"))
                }
                else -> {}
            }
        }
    }

    sealed class LoginEvent {
        object Success : LoginEvent()
        data class Error(val message: String) : LoginEvent()
    }
}
