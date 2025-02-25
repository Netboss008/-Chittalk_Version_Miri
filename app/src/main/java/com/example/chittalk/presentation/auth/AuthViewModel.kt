package com.example.chittalk.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chittalk.data.repository.MatrixRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val matrixRepository: MatrixRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> {
                _authState.update { it.copy(email = event.email) }
            }
            is AuthEvent.PasswordChanged -> {
                _authState.update { it.copy(password = event.password) }
            }
            is AuthEvent.UsernameChanged -> {
                _authState.update { it.copy(username = event.username) }
            }
            AuthEvent.Login -> login()
            AuthEvent.SignUp -> signUp()
            AuthEvent.Logout -> logout()
        }
    }

    private fun login() {
        viewModelScope.launch {
            _authState.update { it.copy(
                isLoading = true,
                error = null
            ) }
            
            matrixRepository.login(
                username = _authState.value.username,
                password = _authState.value.password
            ).collect { result ->
                result.fold(
                    onSuccess = { session ->
                        _authState.update { 
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _authState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Unbekannter Fehler bei der Anmeldung"
                            )
                        }
                    }
                )
            }
        }
    }

    private fun signUp() {
        // Implementierung der Registrierung wird später hinzugefügt
        _authState.update {
            it.copy(
                isLoading = false,
                error = "Registrierung noch nicht implementiert"
            )
        }
    }

    private fun logout() {
        viewModelScope.launch {
            matrixRepository.logout()
            _authState.update {
                it.copy(
                    isAuthenticated = false,
                    email = "",
                    password = "",
                    username = "",
                    error = null
                )
            }
        }
    }
}

data class AuthState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

sealed class AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent()
    data class PasswordChanged(val password: String) : AuthEvent()
    data class UsernameChanged(val username: String) : AuthEvent()
    object Login : AuthEvent()
    object SignUp : AuthEvent()
    object Logout : AuthEvent()
}