package edu.cit.abel.washq.feature.user.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import edu.cit.abel.washq.R
import edu.cit.abel.washq.feature.auth.ui.LoginActivity
import edu.cit.abel.washq.feature.booking.ui.BookingsActivity
import edu.cit.abel.washq.feature.dashboard.ui.DashboardActivity
import edu.cit.abel.washq.shared.api.RetrofitClient
import edu.cit.abel.washq.shared.ui.BaseActivity
import edu.cit.abel.washq.shared.util.SecurePrefsManager
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "My Profile"

        setupBottomNav()
        setupInsets()
        loadProfile()
        setupLogout()
    }

    private fun loadProfile() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val contentView = findViewById<View>(R.id.profileContent)

        progressBar.visibility = View.VISIBLE
        contentView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getProfile()
                if (response.isSuccessful) {
                    val user = response.body()?.data
                    if (user != null) {
                        findViewById<TextView>(R.id.tvName).text = "${user.firstName} ${user.lastName}"
                        findViewById<TextView>(R.id.tvEmail).text = user.email
                        findViewById<TextView>(R.id.tvContact).text = user.contactNumber ?: "Not set"
                        findViewById<TextView>(R.id.tvAddress).text = user.address ?: "Not set"
                        findViewById<TextView>(R.id.tvRole).text = user.role?.uppercase() ?: "CUSTOMER"

                        // Show initials avatar
                        val initials = "${user.firstName?.firstOrNull() ?: ""}${user.lastName?.firstOrNull() ?: ""}"
                        findViewById<TextView>(R.id.tvInitials).text = initials.uppercase()

                        contentView.visibility = View.VISIBLE
                    }
                } else {
                    Snackbar.make(contentView, "Failed to load profile", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Network error: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupLogout() {
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    SecurePrefsManager.clearAuthSession(applicationContext)
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val role = SecurePrefsManager.getUserRole(applicationContext)
        val isStaffOrAdmin = "STAFF".equals(role, ignoreCase = true) || "ADMIN".equals(role, ignoreCase = true)

        if (isStaffOrAdmin) {
            bottomNav.menu.clear()
            bottomNav.inflateMenu(R.menu.bottom_nav_staff_menu)
        }

        bottomNav.selectedItemId = R.id.navProfile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> {
                    if (!isStaffOrAdmin) {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.navBookings -> {
                    startActivity(Intent(this, BookingsActivity::class.java))
                    finish()
                    true
                }
                R.id.navProfile -> true
                else -> false
            }
        }
    }

    private fun setupInsets() {
        val root = findViewById<View>(R.id.profileRoot)
        val bottomNav = findViewById<View>(R.id.bottomNav)
        val contentFrame = findViewById<View>(R.id.contentFrame)
        val appBar = findViewById<View>(R.id.appBarLayout)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            appBar.setPadding(
                appBar.paddingLeft,
                systemBars.top,
                appBar.paddingRight,
                appBar.paddingBottom
            )

            bottomNav.setPadding(
                bottomNav.paddingLeft,
                bottomNav.paddingTop,
                bottomNav.paddingRight,
                systemBars.bottom
            )

            val navHeightPx = (64 * resources.displayMetrics.density).toInt()
            val extraBottomPx = (16 * resources.displayMetrics.density).toInt()
            contentFrame.setPadding(
                contentFrame.paddingLeft,
                contentFrame.paddingTop,
                contentFrame.paddingRight,
                navHeightPx + systemBars.bottom + extraBottomPx
            )

            insets
        }
        ViewCompat.requestApplyInsets(root)
    }
}
