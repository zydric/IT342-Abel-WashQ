package edu.cit.abel.washq.feature.auth.ui

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
import edu.cit.abel.washq.shared.api.RetrofitClient
import edu.cit.abel.washq.feature.auth.model.LoginRequest
import edu.cit.abel.washq.feature.auth.repository.AuthRepository
import edu.cit.abel.washq.shared.util.SecurePrefsManager
import edu.cit.abel.washq.feature.auth.viewmodel.AuthUiState
import edu.cit.abel.washq.feature.auth.viewmodel.AuthViewModel
import edu.cit.abel.washq.feature.auth.viewmodel.AuthViewModelFactory
import edu.cit.abel.washq.shared.ui.BaseActivity
import edu.cit.abel.washq.feature.dashboard.ui.DashboardActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import edu.cit.abel.washq.feature.auth.model.GoogleAuthRequest

class LoginActivity : BaseActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var pbLogin: View

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        setContentView(R.layout.activity_login)
        
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot)) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            
            val icon = findViewById<View>(R.id.ivHeroIcon)
            val params = icon.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.topMargin = systemBars.top + (72 * view.resources.displayMetrics.density).toInt()
            icon.layoutParams = params
            
            val scrollView = view as? android.view.ViewGroup
            val nestedScroll = scrollView?.getChildAt(3) // Index 3 is the NestedScrollView based on our XML structure
            nestedScroll?.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

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

        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<View>(R.id.btnGoogleLogin).setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                loginWithGoogle(idToken)
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Google Login Failed: Missing ID Token", Snackbar.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            val statusCode = e.statusCode
            Snackbar.make(findViewById(android.R.id.content), "Google Login Failed (Code: $statusCode)", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun loginWithGoogle(idToken: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.googleLogin(GoogleAuthRequest(idToken))
                if (response.isSuccessful && response.body()?.success == true) {
                    showLoading(false)
                    val payload = response.body()!!.data
                    if (payload != null) {
                        SecurePrefsManager.saveAuthSession(
                            context = applicationContext,
                            token = payload.accessToken,
                            userId = payload.user.id,
                            userEmail = payload.user.email,
                            userFirstName = payload.user.firstName
                        )
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Invalid response data from server", Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    showLoading(false)
                    val errorMsg = response.errorBody()?.string() ?: "Google sign-in verification failed"
                    Snackbar.make(findViewById(android.R.id.content), errorMsg, Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Snackbar.make(findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
