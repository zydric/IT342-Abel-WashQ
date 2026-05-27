package edu.cit.abel.washq.feature.booking.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import edu.cit.abel.washq.R
import edu.cit.abel.washq.feature.booking.model.BookingResponse
import edu.cit.abel.washq.feature.dashboard.ui.DashboardActivity
import edu.cit.abel.washq.feature.user.ui.ProfileActivity
import edu.cit.abel.washq.shared.api.RetrofitClient
import edu.cit.abel.washq.shared.ui.BaseActivity
import kotlinx.coroutines.launch

class BookingsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "My Bookings"

        recyclerView = findViewById(R.id.rvBookings)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupBottomNav()
        setupInsets()
        fetchBookings()
    }

    private fun fetchBookings() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getBookings()
                if (response.isSuccessful) {
                    val bookings = response.body()?.data ?: emptyList()
                    if (bookings.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                    } else {
                        recyclerView.adapter = BookingAdapter(bookings)
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    Snackbar.make(recyclerView, "Failed to load bookings", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(recyclerView, "Network error: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navBookings
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.navBookings -> true
                R.id.navProfile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupInsets() {
        val root = findViewById<View>(R.id.bookingsRoot)
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

// ── Booking Adapter ─────────────────────────────────────────────────────────
class BookingAdapter(
    private val bookings: List<BookingResponse>
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {

    private val statusColors = mapOf(
        "PENDING" to 0xFF94A3B8.toInt(),
        "RECEIVED" to 0xFF3B82F6.toInt(),
        "IN_PROGRESS" to 0xFFF59E0B.toInt(),
        "READY_FOR_PICKUP" to 0xFF16A34A.toInt(),
        "COMPLETED" to 0xFF059669.toInt(),
        "CANCELLED" to 0xFFDC2626.toInt()
    )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.bookingCard)
        val serviceName: TextView = view.findViewById(R.id.tvBookingService)
        val dateTime: TextView = view.findViewById(R.id.tvBookingDateTime)
        val status: TextView = view.findViewById(R.id.tvBookingStatus)
        val amount: TextView = view.findViewById(R.id.tvBookingAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]
        holder.serviceName.text = booking.service?.name ?: "Laundry Service"
        holder.dateTime.text = "${booking.timeSlot?.slotDate ?: ""} · ${booking.timeSlot?.startTime ?: ""}"
        holder.status.text = booking.status.replace("_", " ")
        holder.status.setTextColor(statusColors[booking.status] ?: 0xFF94A3B8.toInt())
        holder.amount.text = "₱${String.format("%.2f", booking.totalAmount ?: 0.0)}"
    }

    override fun getItemCount() = bookings.size
}
