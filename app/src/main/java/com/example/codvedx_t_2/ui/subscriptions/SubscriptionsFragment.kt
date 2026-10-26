package com.example.codvedx_t_2.ui.subscriptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codvedx_t_2.data.local.AppDatabase
import com.example.codvedx_t_2.data.local.entity.SubscriptionEntity
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.data.repository.ExpenseRepository
import com.example.codvedx_t_2.databinding.FragmentSubscriptionsBinding
import com.example.codvedx_t_2.databinding.ItemSubscriptionBinding
import kotlinx.coroutines.launch
import java.util.Locale

class SubscriptionsFragment : Fragment() {

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ExpenseRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var adapter: SubscriptionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        repository = ExpenseRepository(AppDatabase.getInstance(requireContext()))
        userPreferences = UserPreferences(requireContext())

        adapter = SubscriptionsAdapter(userPreferences.currencySymbol) { sub ->
            showEditSubscriptionDialog(sub)
        }
        binding.rvSubscriptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubscriptions.adapter = adapter

        binding.btnAddSubscription.setOnClickListener {
            showAddSubscriptionDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allSubscriptions.collect { list ->
                adapter.submitList(list)
                val totalCost = list.sumOf { it.amount }
                binding.tvTotalSubscriptionCost.text = "${userPreferences.currencySymbol} ${String.format(Locale.getDefault(), "%.0f", totalCost)} / month"
            }
        }
    }

    private fun showAddSubscriptionDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etName = EditText(requireContext()).apply { hint = "Subscription Name (e.g. Spotify)" }
        val etAmount = EditText(requireContext()).apply { hint = "Monthly Amount" }
        val etPaymentMethod = EditText(requireContext()).apply { hint = "Payment Method" }

        layout.addView(etName)
        layout.addView(etAmount)
        layout.addView(etPaymentMethod)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Subscription")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                val method = etPaymentMethod.text.toString().trim()

                if (name.isNotEmpty() && amount > 0) {
                    lifecycleScope.launch {
                        repository.insertSubscription(
                            SubscriptionEntity(
                                name = name,
                                amount = amount,
                                billingCycle = "Monthly",
                                nextBillingDate = System.currentTimeMillis() + (30L * 24 * 3600 * 1000),
                                paymentMethod = if (method.isEmpty()) "Credit Card" else method
                            )
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Name & Amount are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditSubscriptionDialog(subscription: SubscriptionEntity) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etName = EditText(requireContext()).apply {
            hint = "Subscription Name"
            setText(subscription.name)
        }
        val etAmount = EditText(requireContext()).apply {
            hint = "Monthly Amount"
            setText(subscription.amount.toString())
        }
        val etPaymentMethod = EditText(requireContext()).apply {
            hint = "Payment Method"
            setText(subscription.paymentMethod)
        }

        layout.addView(etName)
        layout.addView(etAmount)
        layout.addView(etPaymentMethod)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Subscription")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val name = etName.text.toString().trim()
                val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                val method = etPaymentMethod.text.toString().trim()

                if (name.isNotEmpty() && amount > 0) {
                    lifecycleScope.launch {
                        repository.updateSubscription(
                            subscription.copy(
                                name = name,
                                amount = amount,
                                paymentMethod = if (method.isEmpty()) "Credit Card" else method
                            )
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Name & Amount are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteSubscription(subscription)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SubscriptionsAdapter(
    private val currencySymbol: String,
    private val onItemClick: (SubscriptionEntity) -> Unit = {}
) : RecyclerView.Adapter<SubscriptionsAdapter.ViewHolder>() {
    private var items = listOf<SubscriptionEntity>()

    fun submitList(list: List<SubscriptionEntity>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemSubscriptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SubscriptionEntity) {
            binding.tvSubName.text = item.name
            binding.tvSubDetails.text = "${item.billingCycle} • ${item.paymentMethod}"
            binding.tvSubAmount.text = "$currencySymbol ${String.format(Locale.getDefault(), "%.0f", item.amount)}/mo"

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
