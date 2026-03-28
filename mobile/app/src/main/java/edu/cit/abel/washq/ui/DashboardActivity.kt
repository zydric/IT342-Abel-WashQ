package edu.cit.abel.washq.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import edu.cit.abel.washq.R
import edu.cit.abel.washq.api.ApiConfig

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val prefs = getSharedPreferences(ApiConfig.PREFS_NAME, Context.MODE_PRIVATE)
        val firstName = prefs.getString(ApiConfig.USER_FIRST_NAME_KEY, getString(R.string.user_default_name)).orEmpty()

        val welcomeText = getString(R.string.dashboard_welcome, firstName)
        findViewById<TextView>(R.id.tvWelcome).text = welcomeText

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
