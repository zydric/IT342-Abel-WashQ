package edu.cit.abel.washq.feature.dashboard.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cit.abel.washq.R
import edu.cit.abel.washq.feature.booking.ui.BookingsActivity
import edu.cit.abel.washq.feature.catalog.ui.ServiceListActivity
import edu.cit.abel.washq.feature.user.ui.ProfileActivity
import edu.cit.abel.washq.shared.ui.BaseActivity
import edu.cit.abel.washq.shared.util.SecurePrefsManager
import androidx.lifecycle.lifecycleScope
import edu.cit.abel.washq.shared.api.RetrofitClient
import kotlinx.coroutines.launch
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
        setupLiveWeather()
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
            com.google.android.material.snackbar.Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.msg_feature_coming_soon),
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
        }

        // Book Now → Service List
        findViewById<View>(R.id.btnBookNow).setOnClickListener {
            startActivity(Intent(this, ServiceListActivity::class.java))
        }

        // Service cards → Service List
        findViewById<View>(R.id.cardRegularWash).setOnClickListener {
            startActivity(Intent(this, ServiceListActivity::class.java))
        }
        findViewById<View>(R.id.cardDryCleaning).setOnClickListener {
            startActivity(Intent(this, ServiceListActivity::class.java))
        }
        findViewById<View>(R.id.cardExpressWash).setOnClickListener {
            startActivity(Intent(this, ServiceListActivity::class.java))
        }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navHome
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> true
                R.id.navBookings -> {
                    startActivity(Intent(this, BookingsActivity::class.java))
                    finish()
                    true
                }
                R.id.navProfile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
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

    private fun setupLiveWeather() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getLiveWeather()
                if (response.isSuccessful && response.body()?.success == true) {
                    val weather = response.body()?.data
                    if (weather != null) {
                        findViewById<TextView>(R.id.tvTemperature).text = "${weather.temperature.toInt()}°"
                        findViewById<TextView>(R.id.tvCondition).text = weather.condition
                        findViewById<TextView>(R.id.tvHumidity).text = "${weather.humidity}% Humidity"

                        val advice = when (weather.condition.lowercase(Locale.ENGLISH)) {
                            "clear" -> "Perfect laundry weather! Hang your clothes outside."
                            "clouds" -> "Good day for washing, but keep an eye on the sky."
                            "rain", "drizzle", "thunderstorm" -> "Rainy day! Use WashQ's premium machine drying."
                            else -> "Great time to schedule a pickup or drop-off."
                        }
                        findViewById<TextView>(R.id.tvWeatherAdvice).text = advice
                    }
                }
            } catch (e: Exception) {
                // Network error fallback: keep the clean layout defaults
            }
        }
    }
}
