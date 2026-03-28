package edu.cit.abel.washq.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import edu.cit.abel.washq.R
import edu.cit.abel.washq.util.SecurePrefsManager

class DashboardActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        applyEdgeToEdgeInsets(findViewById(R.id.dashboardRoot))

        val firstName = SecurePrefsManager.getFirstName(applicationContext, getString(R.string.user_default_name))

        val welcomeText = getString(R.string.dashboard_welcome, firstName)
        findViewById<TextView>(R.id.tvWelcome).text = welcomeText

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            SecurePrefsManager.clearAuthSession(applicationContext)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
