package com.example.codvedx_t_2.ui.goals

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
import com.example.codvedx_t_2.data.local.entity.GoalEntity
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.data.repository.ExpenseRepository
import com.example.codvedx_t_2.databinding.FragmentGoalsBinding
import com.example.codvedx_t_2.databinding.ItemGoalBinding
import kotlinx.coroutines.launch
import java.util.Locale

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ExpenseRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var adapter: GoalsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        repository = ExpenseRepository(AppDatabase.getInstance(requireContext()))
        userPreferences = UserPreferences(requireContext())

        adapter = GoalsAdapter(userPreferences.currencySymbol) { goal ->
            showEditGoalDialog(goal)
        }
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGoals.adapter = adapter

        binding.btnAddGoal.setOnClickListener {
            showAddGoalDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allGoals.collect { goals ->
                adapter.submitList(goals)
            }
        }
    }

    private fun showAddGoalDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etName = EditText(requireContext()).apply { hint = "Goal Name (e.g. Vacation)" }
        val etTarget = EditText(requireContext()).apply { hint = "Target Amount" }
        val etSaved = EditText(requireContext()).apply { hint = "Current Saved Amount" }

        layout.addView(etName)
        layout.addView(etTarget)
        layout.addView(etSaved)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Financial Goal")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val target = etTarget.text.toString().toDoubleOrNull() ?: 0.0
                val saved = etSaved.text.toString().toDoubleOrNull() ?: 0.0

                if (name.isNotEmpty() && target > 0) {
                    lifecycleScope.launch {
                        repository.insertGoal(
                            GoalEntity(
                                name = name,
                                targetAmount = target,
                                currentAmount = saved,
                                targetDate = System.currentTimeMillis() + (180L * 24 * 3600 * 1000)
                            )
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid Goal Input", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditGoalDialog(goal: GoalEntity) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etName = EditText(requireContext()).apply {
            hint = "Goal Name"
            setText(goal.name)
        }
        val etTarget = EditText(requireContext()).apply {
            hint = "Target Amount"
            setText(goal.targetAmount.toString())
        }
        val etSaved = EditText(requireContext()).apply {
            hint = "Current Saved Amount"
            setText(goal.currentAmount.toString())
        }

        layout.addView(etName)
        layout.addView(etTarget)
        layout.addView(etSaved)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Savings Goal")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val name = etName.text.toString().trim()
                val target = etTarget.text.toString().toDoubleOrNull() ?: 0.0
                val saved = etSaved.text.toString().toDoubleOrNull() ?: 0.0

                if (name.isNotEmpty() && target > 0) {
                    lifecycleScope.launch {
                        repository.updateGoal(
                            goal.copy(
                                name = name,
                                targetAmount = target,
                                currentAmount = saved
                            )
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid Goal Input", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteGoal(goal)
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

class GoalsAdapter(
    private val currencySymbol: String,
    private val onItemClick: (GoalEntity) -> Unit = {}
) : RecyclerView.Adapter<GoalsAdapter.ViewHolder>() {
    private var items = listOf<GoalEntity>()

    fun submitList(list: List<GoalEntity>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemGoalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GoalEntity) {
            binding.tvGoalName.text = item.name
            val pct = if (item.targetAmount > 0) ((item.currentAmount / item.targetAmount) * 100).toInt() else 0
            binding.tvGoalPct.text = "$pct%"
            binding.progressGoal.progress = pct.coerceAtMost(100)

            binding.tvGoalAmounts.text = "Saved: $currencySymbol${String.format(Locale.getDefault(), "%.0f", item.currentAmount)} / $currencySymbol${String.format(Locale.getDefault(), "%.0f", item.targetAmount)}"
            val rem = (item.targetAmount - item.currentAmount).coerceAtLeast(0.0)
            binding.tvGoalRemaining.text = "Remaining: $currencySymbol${String.format(Locale.getDefault(), "%.0f", rem)}"

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
