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
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.cit.abel.washq.R
import edu.cit.abel.washq.api.RetrofitClient
import edu.cit.abel.washq.model.RegisterRequest
import edu.cit.abel.washq.repository.AuthRepository
import edu.cit.abel.washq.util.SecurePrefsManager
import edu.cit.abel.washq.viewmodel.AuthUiState
import edu.cit.abel.washq.viewmodel.AuthViewModel
import edu.cit.abel.washq.viewmodel.AuthViewModelFactory

class RegisterActivity : BaseActivity() {

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
    private lateinit var btnRegister: MaterialButton
    private lateinit var pbRegister: View
    private lateinit var strengthTrack: View
    private lateinit var strengthBar: View

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        setContentView(R.layout.activity_register)
        
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerRoot)) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            
            val btnBack = findViewById<View>(R.id.btnBack)
            val params = btnBack.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.topMargin = systemBars.top + (48 * view.resources.displayMetrics.density).toInt() // 48dp base margin from XML plus status bar
            btnBack.layoutParams = params
            
            val scrollView = view as? android.view.ViewGroup
            val nestedScroll = scrollView?.getChildAt(3)
            nestedScroll?.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        bindViews()
        styleSignInLink()
        setupFieldValidation()
        setupPasswordStrengthBar()
        observeRegisterState()

        btnRegister.setOnClickListener {
            submitRegistration()
        }

        findViewById<View>(R.id.btnBack).setOnClickListener {
            navigateToLogin(clearStack = true)
        }

        findViewById<View>(R.id.btnGoToLogin).setOnClickListener {
            navigateToLogin(clearStack = true)
        }

        findViewById<View>(R.id.btnGoogleRegister).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_feature_coming_soon), Snackbar.LENGTH_SHORT).show()
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
        btnRegister = findViewById(R.id.btnRegister)
        pbRegister = findViewById(R.id.pbRegister)
        strengthTrack = findViewById(R.id.passwordStrengthTrack)
        strengthBar = findViewById(R.id.passwordStrengthBar)
    }

    private fun styleSignInLink() {
        val target = findViewById<android.widget.TextView>(R.id.btnGoToLogin)
        val fullText = getString(R.string.text_have_account_sign_in)
        val emphasize = getString(R.string.text_sign_in_word)
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
        etFirstName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateFirstName(showError = true)
        }
        etLastName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateLastName(showError = true)
        }
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateEmail(showError = true)
        }
        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validatePassword(showError = true)
        }
    }

    private fun setupPasswordStrengthBar() {
        etPassword.doAfterTextChanged {
            val password = it?.toString().orEmpty()
            updatePasswordStrength(password)
        }
    }

    private fun observeRegisterState() {
        viewModel.registerState.observe(this) { state ->
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
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_account_created_email), 4000)
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                val intent = Intent(this@RegisterActivity, DashboardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        })
                        .show()
                }
                is AuthUiState.Error -> {
                    showLoading(false)
                    if (state.code == "DB-002") {
                        tilEmail.error = getString(R.string.msg_duplicate_email)
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_duplicate_email), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.action_sign_in_caps)) {
                                navigateToLogin(clearStack = true)
                            }
                            .show()
                    } else {
                        val message = when (state.code) {
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
    }

    private fun submitRegistration() {
        clearErrors()

        val firstNameValid = validateFirstName(showError = true)
        val lastNameValid = validateLastName(showError = true)
        val emailValid = validateEmail(showError = true)
        val passwordValid = validatePassword(showError = true)

        if (!firstNameValid || !lastNameValid || !emailValid || !passwordValid) {
            return
        }

        val firstName = etFirstName.text?.toString()?.trim().orEmpty()
        val lastName = etLastName.text?.toString()?.trim().orEmpty()
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString().orEmpty()
        val address = etAddress.text?.toString()?.trim().orEmpty()
        val contact = etContact.text?.toString()?.trim().orEmpty()

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

    private fun validateFirstName(showError: Boolean): Boolean {
        val value = etFirstName.text?.toString()?.trim().orEmpty()
        return if (value.isBlank()) {
            if (showError) tilFirstName.error = getString(R.string.err_first_name_required)
            false
        } else {
            tilFirstName.error = null
            true
        }
    }

    private fun validateLastName(showError: Boolean): Boolean {
        val value = etLastName.text?.toString()?.trim().orEmpty()
        return if (value.isBlank()) {
            if (showError) tilLastName.error = getString(R.string.err_last_name_required)
            false
        } else {
            tilLastName.error = null
            true
        }
    }

    private fun validateEmail(showError: Boolean): Boolean {
        val value = etEmail.text?.toString()?.trim().orEmpty()
        return when {
            value.isBlank() -> {
                if (showError) tilEmail.error = getString(R.string.err_email_required)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> {
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
        val value = etPassword.text?.toString().orEmpty()
        return when {
            value.isBlank() -> {
                if (showError) tilPassword.error = getString(R.string.err_password_required)
                false
            }
            value.length < 8 -> {
                if (showError) tilPassword.error = getString(R.string.err_password_min_8)
                false
            }
            else -> {
                tilPassword.error = null
                true
            }
        }
    }

    private fun updatePasswordStrength(password: String) {
        val score = calculatePasswordScore(password)
        val widthFactor = when (score) {
            0 -> 0f
            1 -> 0.33f
            2 -> 0.66f
            else -> 1f
        }

        val colorRes = when (score) {
            1 -> R.color.error
            2 -> R.color.warning
            3 -> R.color.success
            else -> R.color.surface_variant
        }
        strengthBar.setBackgroundColor(ContextCompat.getColor(this, colorRes))

        strengthTrack.post {
            val parentWidth = strengthTrack.width
            val targetWidth = (parentWidth * widthFactor).toInt()
            val params = strengthBar.layoutParams
            params.width = targetWidth
            strengthBar.layoutParams = params
        }
    }

    private fun calculatePasswordScore(password: String): Int {
        if (password.isBlank()) {
            return 0
        }

        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isDigit() } && password.any { it.isLetter() }) score++
        if (password.length >= 12 || password.any { !it.isLetterOrDigit() }) score++
        return score.coerceAtMost(3)
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
        if (loading) {
            hideKeyboard()
        }
        pbRegister.visibility = if (loading) View.VISIBLE else View.GONE
        btnRegister.text = if (loading) "" else getString(R.string.action_create_account)
        btnRegister.isEnabled = !loading
        findViewById<View>(R.id.btnGoogleRegister).isEnabled = !loading
        findViewById<View>(R.id.btnGoToLogin).isEnabled = !loading
        findViewById<View>(R.id.btnBack).isEnabled = !loading
        etFirstName.isEnabled = !loading
        etLastName.isEnabled = !loading
        etEmail.isEnabled = !loading
        etPassword.isEnabled = !loading
        etAddress.isEnabled = !loading
        etContact.isEnabled = !loading
    }

    private fun hideKeyboard() {
        val imm = getSystemService<InputMethodManager>() ?: return
        val target = currentFocus ?: window.decorView
        imm.hideSoftInputFromWindow(target.windowToken, 0)
    }

    private fun navigateToLogin(clearStack: Boolean) {
        val intent = Intent(this, LoginActivity::class.java)
        if (clearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
