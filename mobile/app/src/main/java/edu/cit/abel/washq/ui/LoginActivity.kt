package edu.cit.abel.washq.ui

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.button.MaterialButton
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
    private lateinit var btnLogin: MaterialButton
    private lateinit var pbLogin: View

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        setContentView(R.layout.activity_login)
        applyEdgeToEdgeInsets(findViewById(R.id.loginRoot))

        bindViews()
        styleRegisterLink()
        setupFieldValidation()
        observeLoginState()

        btnLogin.setOnClickListener {
            submitLogin()
        }

        findViewById<View>(R.id.btnGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<View>(R.id.btnForgotPassword).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_feature_coming_soon), Snackbar.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnGoogleLogin).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_feature_coming_soon), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun bindViews() {
        tilEmail = findViewById(R.id.tilLoginEmail)
        tilPassword = findViewById(R.id.tilLoginPassword)
        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        pbLogin = findViewById(R.id.pbLogin)
    }

    private fun styleRegisterLink() {
        val target = findViewById<android.widget.TextView>(R.id.btnGoToRegister)
        val fullText = getString(R.string.text_no_account_register)
        val emphasize = getString(R.string.text_register_word)
        val start = fullText.indexOf(emphasize)

        if (start >= 0) {
            val spannable = SpannableString(fullText)
            val end = start + emphasize.length
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            target.text = spannable
        }
    }

    private fun setupFieldValidation() {
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateEmail(showError = true)
            }
        }
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validatePassword(showError = true)
            }
        }
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
                    if (state.code == "AUTH-001") {
                        tilEmail.error = getString(R.string.err_invalid_credentials_inline)
                        tilPassword.error = getString(R.string.err_invalid_credentials_inline)
                    }
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.action_dismiss)) { }
                        .show()
                }
            }
        }
    }

    private fun submitLogin() {
        clearErrors()

        val emailValid = validateEmail(showError = true)
        val passwordValid = validatePassword(showError = true)
        if (!emailValid || !passwordValid) {
            return
        }

        val email = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()
        viewModel.login(LoginRequest(email = email, password = password))
    }

    private fun validateEmail(showError: Boolean): Boolean {
        val email = etEmail.text?.toString()?.trim().orEmpty()
        return when {
            email.isBlank() -> {
                if (showError) tilEmail.error = getString(R.string.err_email_required)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                if (showError) tilEmail.error = getString(R.string.err_email_invalid)
                false
            }
            else -> {
                tilEmail.error = null
                true
            }
        }
    }

    private fun validatePassword(showError: Boolean): Boolean {
        val password = etPassword.text?.toString().orEmpty()
        return if (password.isBlank()) {
            if (showError) tilPassword.error = getString(R.string.err_password_required)
            false
        } else {
            tilPassword.error = null
            true
        }
    }

    private fun clearErrors() {
        tilEmail.error = null
        tilPassword.error = null
    }

    private fun showLoading(loading: Boolean) {
        if (loading) {
            hideKeyboard()
        }
        pbLogin.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.text = if (loading) "" else getString(R.string.action_sign_in)
        btnLogin.isEnabled = !loading
        findViewById<View>(R.id.btnGoToRegister).isEnabled = !loading
        findViewById<View>(R.id.btnForgotPassword).isEnabled = !loading
        findViewById<View>(R.id.btnGoogleLogin).isEnabled = !loading
        etEmail.isEnabled = !loading
        etPassword.isEnabled = !loading
    }

    private fun hideKeyboard() {
        val imm = getSystemService<InputMethodManager>() ?: return
        val target = currentFocus ?: window.decorView
        imm.hideSoftInputFromWindow(target.windowToken, 0)
    }
}
