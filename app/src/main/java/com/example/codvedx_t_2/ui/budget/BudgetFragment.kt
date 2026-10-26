package com.example.codvedx_t_2.ui.budget

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codvedx_t_2.data.local.entity.CategoryEntity
import com.example.codvedx_t_2.databinding.FragmentBudgetBinding
import java.util.Locale

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var adapter: CategoryBudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val symbol = viewModel.userPreferences.currencySymbol
        adapter = CategoryBudgetAdapter(symbol) { uiModel ->
            showEditCategoryBudgetDialog(uiModel.category)
        }

        binding.rvCategoryBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryBudgets.adapter = adapter

        binding.btnEditBudget.setOnClickListener {
            showEditOverallBudgetDialog()
        }

        observeData(symbol)
    }

    private fun observeData(symbol: String) {
        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            val totalSpent = expense ?: 0.0
            val budget = viewModel.userPreferences.monthlyBudget

            binding.tvTotalBudgetAmount.text = "$symbol ${String.format(Locale.getDefault(), "%.0f", budget)}"
            binding.tvSpentAmount.text = "Spent: $symbol${String.format(Locale.getDefault(), "%.0f", totalSpent)}"

            val remaining = (budget - totalSpent).coerceAtLeast(0.0)
            binding.tvRemainingAmount.text = "Remaining: $symbol${String.format(Locale.getDefault(), "%.0f", remaining)}"

            val pct = if (budget > 0) ((totalSpent / budget) * 100).toInt() else 0
            binding.progressOverallBudget.progress = pct.coerceAtMost(100)
        }

        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val expenseCategories = categories.filter { it.type == "EXPENSE" }
            viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
                val uiModels = expenseCategories.map { cat ->
                    val spent = transactions.filter { it.categoryId == cat.id && it.type == "EXPENSE" }
                        .sumOf { it.amount }
                    CategoryBudgetUiModel(cat, spent)
                }
                adapter.submitList(uiModels)
            }
        }
    }

    private fun showEditOverallBudgetDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(viewModel.userPreferences.monthlyBudget.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Set Total Monthly Budget")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newBudget = input.text.toString().toDoubleOrNull() ?: 0.0
                viewModel.updateMonthlyBudget(newBudget)
                Toast.makeText(requireContext(), "Total budget updated", Toast.LENGTH_SHORT).show()
                observeData(viewModel.userPreferences.currencySymbol)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditCategoryBudgetDialog(category: CategoryEntity) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(category.budget.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Set Budget for ${category.name}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newBudget = input.text.toString().toDoubleOrNull() ?: 0.0
                viewModel.updateCategoryBudget(category, newBudget)
                Toast.makeText(requireContext(), "${category.name} budget updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
