package edu.cit.abel.washq.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.cit.abel.washq.R
import edu.cit.abel.washq.api.RetrofitClient
import edu.cit.abel.washq.model.RegisterRequest
import edu.cit.abel.washq.repository.AuthRepository
import edu.cit.abel.washq.viewmodel.AuthUiState
import edu.cit.abel.washq.viewmodel.AuthViewModel
import edu.cit.abel.washq.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilFirstName: TextInputLayout
    private lateinit var tilLastName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilAddress: TextInputLayout
    private lateinit var tilContact: TextInputLayout

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etContact: TextInputEditText

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        setContentView(R.layout.activity_register)

        bindViews()
        observeRegisterState()

        findViewById<View>(R.id.btnRegister).setOnClickListener {
            submitRegistration()
        }
    }

    private fun bindViews() {
        tilFirstName = findViewById(R.id.tilFirstName)
        tilLastName = findViewById(R.id.tilLastName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilAddress = findViewById(R.id.tilAddress)
        tilContact = findViewById(R.id.tilContactNumber)

        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etAddress = findViewById(R.id.etAddress)
        etContact = findViewById(R.id.etContactNumber)
    }

    private fun observeRegisterState() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is AuthUiState.Loading -> showLoading(true)
                is AuthUiState.Success -> {
                    showLoading(false)
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_registration_success), Snackbar.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is AuthUiState.Error -> {
                    showLoading(false)
                    val message = when (state.code) {
                        "DB-002" -> getString(R.string.msg_duplicate_email)
                        "NETWORK-001" -> getString(R.string.msg_no_internet)
                        "VALID-001" -> state.message.ifBlank { getString(R.string.msg_request_failed) }
                        "SYSTEM-001" -> getString(R.string.msg_unexpected_error)
                        else -> state.message.ifBlank { getString(R.string.msg_request_failed) }
                    }
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun submitRegistration() {
        clearErrors()

        val firstName = etFirstName.text?.toString()?.trim().orEmpty()
        val lastName = etLastName.text?.toString()?.trim().orEmpty()
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()
        val address = etAddress.text?.toString()?.trim().orEmpty()
        val contact = etContact.text?.toString()?.trim().orEmpty()

        var hasError = false

        if (firstName.isBlank()) {
            tilFirstName.error = getString(R.string.err_first_name_required)
            hasError = true
        }

        if (lastName.isBlank()) {
            tilLastName.error = getString(R.string.err_last_name_required)
            hasError = true
        }

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
        } else if (password.length < 8) {
            tilPassword.error = getString(R.string.err_password_min_8)
            hasError = true
        }

        if (hasError) {
            return
        }

        viewModel.register(
            RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                address = address.ifBlank { null },
                contactNumber = contact.ifBlank { null }
            )
        )
    }

    private fun clearErrors() {
        tilFirstName.error = null
        tilLastName.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilAddress.error = null
        tilContact.error = null
    }

    private fun showLoading(loading: Boolean) {
        findViewById<View>(R.id.pbRegister).visibility = if (loading) View.VISIBLE else View.GONE
        findViewById<View>(R.id.btnRegister).isEnabled = !loading
    }
}
