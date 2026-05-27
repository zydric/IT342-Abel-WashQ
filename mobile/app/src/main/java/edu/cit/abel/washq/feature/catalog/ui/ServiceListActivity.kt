package edu.cit.abel.washq.feature.catalog.ui

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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import edu.cit.abel.washq.R
import edu.cit.abel.washq.feature.catalog.model.ServiceDto
import edu.cit.abel.washq.feature.timeslot.ui.SlotSelectionActivity
import edu.cit.abel.washq.shared.api.RetrofitClient
import edu.cit.abel.washq.shared.ui.BaseActivity
import kotlinx.coroutines.launch

class ServiceListActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.rvServices)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupInsets()
        fetchServices()
    }

    private fun fetchServices() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getInstance(applicationContext).getServices()
                if (response.isSuccessful) {
                    val services = response.body()?.data ?: emptyList()
                    val active = services.filter { it.isActive }
                    if (active.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                    } else {
                        recyclerView.adapter = ServiceAdapter(active) { service ->
                            val intent = Intent(this@ServiceListActivity, SlotSelectionActivity::class.java).apply {
                                putExtra("serviceId", service.id)
                                putExtra("serviceName", service.name)
                                putExtra("pricePerKg", service.pricePerKg)
                            }
                            startActivity(intent)
                        }
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    Snackbar.make(recyclerView, "Failed to load services", Snackbar.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Snackbar.make(recyclerView, "Network error: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupInsets() {
        val root = findViewById<View>(R.id.serviceListRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }
    }
}

// ── Adapter ─────────────────────────────────────────────────────────────────
class ServiceAdapter(
    private val services: List<ServiceDto>,
    private val onClick: (ServiceDto) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.serviceCard)
        val name: TextView = view.findViewById(R.id.tvServiceName)
        val description: TextView = view.findViewById(R.id.tvServiceDescription)
        val price: TextView = view.findViewById(R.id.tvServicePrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = services[position]
        holder.name.text = service.name
        holder.description.text = service.description ?: ""
        holder.price.text = "₱${String.format("%.2f", service.pricePerKg)}/kg"
        holder.card.setOnClickListener { onClick(service) }
    }

    override fun getItemCount() = services.size
}
