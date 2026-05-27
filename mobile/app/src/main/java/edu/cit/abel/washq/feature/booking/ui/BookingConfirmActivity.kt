package edu.cit.abel.washq.feature.booking.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import edu.cit.abel.washq.R
import edu.cit.abel.washq.feature.booking.model.BookingRequest
import edu.cit.abel.washq.feature.booking.model.PaymentRequest
import edu.cit.abel.washq.shared.api.RetrofitClient
import edu.cit.abel.washq.shared.ui.BaseActivity
import kotlinx.coroutines.launch

class BookingConfirmActivity : BaseActivity() {

    private var serviceId: Long = 0
    private var serviceName: String = ""
    private var pricePerKg: Double = 0.0
    private var slotId: Long = 0
    private var slotDate: String = ""
    private var slotStart: String = ""
    private var slotEnd: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_confirm)

        serviceId = intent.getLongExtra("serviceId", 0)
        serviceName = intent.getStringExtra("serviceName") ?: ""
        pricePerKg = intent.getDoubleExtra("pricePerKg", 0.0)
        slotId = intent.getLongExtra("slotId", 0)
        slotDate = intent.getStringExtra("slotDate") ?: ""
        slotStart = intent.getStringExtra("slotStart") ?: ""
        slotEnd = intent.getStringExtra("slotEnd") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Populate summary
        findViewById<TextView>(R.id.tvServiceName).text = serviceName
        findViewById<TextView>(R.id.tvServicePrice).text = "₱${String.format("%.2f", pricePerKg)}/kg"
        findViewById<TextView>(R.id.tvSlotDate).text = slotDate
        findViewById<TextView>(R.id.tvSlotTime).text = "$slotStart - $slotEnd"

        val etWeight = findViewById<TextInputEditText>(R.id.etWeight)
        val etInstructions = findViewById<TextInputEditText>(R.id.etInstructions)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Live total calculation
        etWeight.setOnFocusChangeListener { _, _ ->
            val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0
            tvTotal.text = "₱${String.format("%.2f", weight * pricePerKg)}"
        }

        btnConfirm.setOnClickListener {
            val weight = etWeight.text.toString().toDoubleOrNull()
            if (weight == null || weight <= 0) {
                etWeight.error = "Enter a valid weight"
                return@setOnClickListener
            }

            tvTotal.text = "₱${String.format("%.2f", weight * pricePerKg)}"

            btnConfirm.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val request = BookingRequest(
                serviceId = serviceId,
                timeSlotId = slotId,
                estimatedWeightKg = weight,
                specialInstructions = etInstructions.text?.toString()?.trim()
            )

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.createBooking(request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val bookingId = response.body()?.data?.id
                        if (bookingId != null) {
                            val payResponse = RetrofitClient.apiService.createPayment(PaymentRequest(bookingId))
                            if (payResponse.isSuccessful && payResponse.body()?.success == true) {
                                val checkoutUrl = payResponse.body()?.data?.checkoutUrl
                                if (!checkoutUrl.isNullOrBlank()) {
                                    // Open PayMongo checkout session directly in the system browser
                                    val browserIntent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(checkoutUrl)
                                    )
                                    startActivity(browserIntent)
                                    finish()
                                    return@launch
                                }
                            }
                        }

                        // Fallback local success dialog if payment URL is somehow blank
                        MaterialAlertDialogBuilder(this@BookingConfirmActivity)
                            .setTitle("Booking Confirmed! ✅")
                            .setMessage("Your $serviceName booking has been created successfully.")
                            .setPositiveButton("View My Bookings") { _, _ ->
                                startActivity(android.content.Intent(this@BookingConfirmActivity, BookingsActivity::class.java))
                                finish()
                            }
                            .setCancelable(false)
                            .show()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Snackbar.make(btnConfirm, "Booking failed: $errorBody", Snackbar.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Snackbar.make(btnConfirm, "Network error: ${e.message}", Snackbar.LENGTH_LONG).show()
                } finally {
                    btnConfirm.isEnabled = true
                    progressBar.visibility = View.GONE
                }
            }
        }

        setupInsets()
    }

    private fun setupInsets() {
        val root = findViewById<View>(R.id.bookingConfirmRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }
    }
}
