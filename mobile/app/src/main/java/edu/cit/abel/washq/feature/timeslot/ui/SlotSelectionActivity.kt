package edu.cit.abel.washq.feature.timeslot.ui

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import edu.cit.abel.washq.R
import edu.cit.abel.washq.feature.booking.ui.BookingConfirmActivity
import edu.cit.abel.washq.feature.timeslot.model.TimeSlotDto
import edu.cit.abel.washq.shared.api.RetrofitClient
import edu.cit.abel.washq.shared.ui.BaseActivity
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class SlotSelectionActivity : BaseActivity() {

    private var serviceId: Long = 0
    private var serviceName: String = ""
    private var pricePerKg: Double = 0.0

    private lateinit var chipGroup: ChipGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slot_selection)

        serviceId = intent.getLongExtra("serviceId", 0)
        serviceName = intent.getStringExtra("serviceName") ?: ""
        pricePerKg = intent.getDoubleExtra("pricePerKg", 0.0)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Select Time Slot"
        toolbar.subtitle = serviceName
        toolbar.setNavigationOnClickListener { finish() }

        chipGroup = findViewById(R.id.chipGroupDates)
        recyclerView = findViewById(R.id.rvSlots)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = GridLayoutManager(this, 2)

        setupInsets()
        setupDateChips()
    }

    private fun setupDateChips() {
        val today = LocalDate.now()
        for (i in 0..6) {
            val date = today.plusDays(i.toLong())
            val chip = Chip(this).apply {
                text = date.format(displayFormatter)
                tag = date.format(dateFormatter)
                isCheckable = true
                isCheckedIconVisible = false
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    if (i == 0) getColor(R.color.primary_light) else getColor(R.color.white)
                )
            }
            chipGroup.addView(chip)

            chip.setOnClickListener {
                fetchSlots(date.format(dateFormatter))
            }

            if (i == 0) {
                chip.isChecked = true
                fetchSlots(date.format(dateFormatter))
            }
        }
    }

    private fun fetchSlots(date: String) {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(applicationContext).getAvailableSlots(date)
                if (response.isSuccessful) {
                    val slots = response.body()?.data ?: emptyList()
                    if (slots.isEmpty()) {
                        emptyView.text = "No available slots for this date"
                        emptyView.visibility = View.VISIBLE
                    } else {
                        recyclerView.adapter = SlotAdapter(slots) { slot ->
                            val intent = Intent(this@SlotSelectionActivity, BookingConfirmActivity::class.java).apply {
                                putExtra("serviceId", serviceId)
                                putExtra("serviceName", serviceName)
                                putExtra("pricePerKg", pricePerKg)
                                putExtra("slotId", slot.id)
                                putExtra("slotDate", slot.slotDate)
                                putExtra("slotStart", slot.startTime)
                                putExtra("slotEnd", slot.endTime)
                            }
                            startActivity(intent)
                        }
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    Snackbar.make(recyclerView, "Failed to load slots", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(recyclerView, "Network error: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupInsets() {
        val root = findViewById<View>(R.id.slotSelectionRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }
    }
}

// ── Slot Adapter ────────────────────────────────────────────────────────────
class SlotAdapter(
    private val slots: List<TimeSlotDto>,
    private val onClick: (TimeSlotDto) -> Unit
) : RecyclerView.Adapter<SlotAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.slotCard)
        val time: TextView = view.findViewById(R.id.tvSlotTime)
        val availability: TextView = view.findViewById(R.id.tvSlotAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = slots[position]
        holder.time.text = "${slot.startTime} - ${slot.endTime}"
        val remaining = slot.maxCapacity - slot.currentBookingCount
        holder.availability.text = "$remaining spots left"

        if (slot.isAvailable) {
            holder.card.setOnClickListener { onClick(slot) }
            holder.card.alpha = 1f
        } else {
            holder.card.alpha = 0.4f
            holder.availability.text = "Full"
            holder.card.isClickable = false
        }
    }

    override fun getItemCount() = slots.size
}
