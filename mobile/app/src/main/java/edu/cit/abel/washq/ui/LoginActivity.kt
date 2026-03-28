package edu.cit.abel.washq.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.cit.abel.washq.R
import edu.cit.abel.washq.api.RetrofitClient
import edu.cit.abel.washq.model.LoginRequest
import edu.cit.abel.washq.repository.AuthRepository
import edu.cit.abel.washq.util.SecurePrefsManager
import edu.cit.abel.washq.viewmodel.AuthUiState
import edu.cit.abel.washq.viewmodel.AuthViewModel
import edu.cit.abel.washq.viewmodel.AuthViewModelFactory

class LoginActivity : BaseActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        setContentView(R.layout.activity_login)
        applyEdgeToEdgeInsets(findViewById(R.id.loginRoot))

        bindViews()
        observeLoginState()

        findViewById<View>(R.id.btnLogin).setOnClickListener {
            submitLogin()
        }

        findViewById<View>(R.id.btnGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun bindViews() {
        tilEmail = findViewById(R.id.tilLoginEmail)
        tilPassword = findViewById(R.id.tilLoginPassword)
        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is AuthUiState.Loading -> showLoading(true)
                is AuthUiState.Success -> {
                    showLoading(false)
                    val payload = state.data
                    SecurePrefsManager.saveAuthSession(
                        context = applicationContext,
                        token = payload.accessToken,
                        userId = payload.user.id,
                        userEmail = payload.user.email,
                        userFirstName = payload.user.firstName
                    )

                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                is AuthUiState.Error -> {
                    showLoading(false)
                    val message = when (state.code) {
                        "AUTH-001" -> getString(R.string.msg_invalid_credentials)
                        "NETWORK-001" -> getString(R.string.msg_no_internet)
                        "SYSTEM-001" -> getString(R.string.msg_unexpected_error)
                        else -> state.message.ifBlank { getString(R.string.msg_request_failed) }
                    }
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun submitLogin() {
        clearErrors()

        val email = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()
        var hasError = false

        if (email.isBlank()) {
            tilEmail.error = getString(R.string.err_email_required)
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = getString(R.string.err_email_invalid)
            hasError = true
        }

        if (password.isBlank()) {
            tilPassword.error = getString(R.string.err_password_required)
            hasError = true
        }

        if (hasError) {
            return
        }

        viewModel.login(LoginRequest(email = email, password = password))
    }

    private fun clearErrors() {
        tilEmail.error = null
        tilPassword.error = null
    }

    private fun showLoading(loading: Boolean) {
        findViewById<View>(R.id.pbLogin).visibility = if (loading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.btnLogin).isEnabled = !loading
        findViewById<View>(R.id.btnGoToRegister).isEnabled = !loading
    }
}
