package pt.isec.a2022143267.safetysec.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isec.a2022143267.safetysec.model.User
import pt.isec.a2022143267.safetysec.model.UserType
import pt.isec.a2022143267.safetysec.repository.AuthRepository

/**
 * ViewModel for authentication operations
 */
class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            if (authRepository.currentUser != null) {
                authRepository.getCurrentUser()
                    .onSuccess { user ->
                        _currentUser.value = user
                        _authState.value = AuthState.Authenticated
                    }
                    .onFailure {
                        _authState.value = AuthState.Idle
                    }
            }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        userType: UserType
    ) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }

        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.register(email, password, name, userType)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password are required")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Login failed")
                }
        }
    }

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Email is required")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.resetPassword(email)
                .onSuccess {
                    _authState.value = AuthState.PasswordResetSent
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Password reset failed")
                }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object PasswordResetSent : AuthState()
    data class Error(val message: String) : AuthState()
}

