package com.kalyangupta.wallet.ui.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.AdminUserDto
import com.kalyangupta.wallet.data.remote.dto.CategoryDto
import com.kalyangupta.wallet.data.repository.AdminRepository
import com.kalyangupta.wallet.data.repository.CategoryRepository
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _usersState = mutableStateOf<AdminState<List<AdminUserDto>>>(AdminState.Loading())
    val usersState: State<AdminState<List<AdminUserDto>>> = _usersState

    private val _categoriesState = mutableStateOf<AdminState<List<CategoryDto>>>(AdminState.Loading())
    val categoriesState: State<AdminState<List<CategoryDto>>> = _categoriesState

    private val _registrationEnabled = mutableStateOf(true)
    val registrationEnabled: State<Boolean> = _registrationEnabled

    init {
        loadAdminData()
    }

    fun loadAdminData() {
        viewModelScope.launch {
            _usersState.value = AdminState.Loading()
            val result = adminRepository.getAdminUsers()
            when (result) {
                is Resource.Success -> _usersState.value = AdminState.Success(result.data ?: emptyList())
                is Resource.Error -> _usersState.value = AdminState.Error(result.message ?: "Unknown error")
                else -> {}
            }
        }
        viewModelScope.launch {
            val result = adminRepository.getRegistrationStatus()
            if (result is Resource.Success) {
                _registrationEnabled.value = result.data ?: true
            }
        }
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = AdminState.Loading()
            val result = categoryRepository.getCategories()
            when (result) {
                is Resource.Success -> _categoriesState.value = AdminState.Success(result.data ?: emptyList())
                is Resource.Error -> _categoriesState.value = AdminState.Error(result.message ?: "Unknown error")
                else -> {}
            }
        }
    }

    fun addCategory(name: String, code: String) {
        viewModelScope.launch {
            val result = categoryRepository.createCategory(CategoryDto(name = name, code = code))
            if (result is Resource.Success) {
                loadCategories()
            }
        }
    }

    fun updateCategory(id: Int, name: String, code: String) {
        viewModelScope.launch {
            val result = categoryRepository.updateCategory(id, CategoryDto(id = id, name = name, code = code))
            if (result is Resource.Success) {
                loadCategories()
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            val result = categoryRepository.deleteCategory(id)
            if (result is Resource.Success) {
                loadCategories()
            }
        }
    }

    fun toggleRegistration() {
        viewModelScope.launch {
            val result = adminRepository.toggleRegistration()
            if (result is Resource.Success) {
                _registrationEnabled.value = result.data ?: true
            }
        }
    }

    fun toggleUserStatus(userId: Int) {
        viewModelScope.launch {
            val result = adminRepository.toggleUserStatus(userId)
            if (result is Resource.Success) {
                loadAdminData()
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            val result = adminRepository.deleteUser(userId)
            if (result is Resource.Success) {
                loadAdminData()
            }
        }
    }

    sealed class AdminState<T>(val data: T? = null, val error: String? = null) {
        class Loading<T> : AdminState<T>()
        class Success<T>(data: T) : AdminState<T>(data = data)
        class Error<T>(message: String) : AdminState<T>(error = message)
    }
}
