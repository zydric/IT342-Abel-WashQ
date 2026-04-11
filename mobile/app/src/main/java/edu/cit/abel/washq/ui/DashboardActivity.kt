package edu.cit.abel.washq.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import edu.cit.abel.washq.R
import edu.cit.abel.washq.util.SecurePrefsManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DashboardActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setupInsets()
        setupToolbar()
        setupDashboardHeader()
        setupActions()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.dashboardToolbar)
        toolbar.title = getString(R.string.app_name)
    }

    private fun setupDashboardHeader() {
        val firstName = SecurePrefsManager.getFirstName(applicationContext, getString(R.string.user_default_name))
        findViewById<TextView>(R.id.tvGreeting).text = getString(R.string.dashboard_greeting, firstName)
        findViewById<TextView>(R.id.tvDate).text = formatCurrentDate()
    }

    private fun setupActions() {
        findViewById<View>(R.id.notificationContainer).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_feature_coming_soon), Snackbar.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnBookNow).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_booking_coming_soon), Snackbar.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.cardRegularWash).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_booking_coming_soon), Snackbar.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.cardDryCleaning).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_booking_coming_soon), Snackbar.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.cardExpressWash).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_booking_coming_soon), Snackbar.LENGTH_SHORT).show()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navHome
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> true
                R.id.navBookings,
                R.id.navProfile -> {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_tab_coming_soon), Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        bottomNav.setOnItemReselectedListener { }
    }

    private fun setupInsets() {
        val root = findViewById<View>(R.id.dashboardRoot)
        val appBar = findViewById<View>(R.id.dashboardAppBar)
        val bottomNav = findViewById<View>(R.id.bottomNav)
        val scroll = findViewById<View>(R.id.dashboardScroll)

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
            val extraBottomPx = (16 * resources.displayMetrics.density).toInt() // breathing 16
            scroll.setPadding(
                scroll.paddingLeft,
                scroll.paddingTop,
                scroll.paddingRight,
                navHeightPx + systemBars.bottom + extraBottomPx
            )

            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun formatCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
        return LocalDate.now().format(formatter)
    }
}
