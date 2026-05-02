package edu.cit.abel.washq.feature.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.abel.washq.feature.auth.model.AuthResponse
import edu.cit.abel.washq.feature.auth.model.LoginRequest
import edu.cit.abel.washq.feature.auth.model.RegisterRequest
import edu.cit.abel.washq.feature.auth.repository.AuthRepository
import edu.cit.abel.washq.shared.model.Result
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableLiveData<AuthUiState>()
    val registerState: LiveData<AuthUiState> = _registerState

    private val _loginState = MutableLiveData<AuthUiState>()
    val loginState: LiveData<AuthUiState> = _loginState

    fun register(request: RegisterRequest) {
        _registerState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.register(request)) {
                is Result.Success -> _registerState.postValue(AuthUiState.Success(result.data))
                is Result.Error -> _registerState.postValue(
                    AuthUiState.Error(
                        code = result.code,
                        message = result.message
                    )
                )
            }
        }
    }

    fun login(request: LoginRequest) {
        _loginState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.login(request)) {
                is Result.Success -> _loginState.postValue(AuthUiState.Success(result.data))
                is Result.Error -> _loginState.postValue(
                    AuthUiState.Error(
                        code = result.code,
                        message = result.message
                    )
                )
            }
        }
    }
}

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data class Success(val data: AuthResponse) : AuthUiState()
    data class Error(val code: String?, val message: String) : AuthUiState()
}
