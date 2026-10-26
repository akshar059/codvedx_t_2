package com.example.codvedx_t_2.ui.transaction

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codvedx_t_2.R
import com.example.codvedx_t_2.data.local.entity.TransactionEntity
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.databinding.FragmentTransactionsListBinding

class TransactionsListFragment : Fragment() {

    private var _binding: FragmentTransactionsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter
    private lateinit var userPreferences: UserPreferences

    private var allList = listOf<TransactionEntity>()
    private var filterType = "ALL"
    private var searchQuery = ""
    private var sortMode = 0 // 0: Newest, 1: Oldest, 2: Highest Amount, 3: Lowest Amount

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        adapter = TransactionAdapter(userPreferences.currencySymbol) { transaction ->
            val bottomSheet = AddEditTransactionBottomSheet.newInstance(transaction)
            bottomSheet.show(parentFragmentManager, "AddEditTransactionBottomSheet")
        }

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        setupSearchAndFilter()
        setupSortButton()
        observeData()
    }

    private fun setupSearchAndFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilterAndSort()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.contains(R.id.chip_income)) {
                filterType = "INCOME"
            } else if (checkedIds.contains(R.id.chip_expense)) {
                filterType = "EXPENSE"
            } else {
                filterType = "ALL"
            }
            applyFilterAndSort()
        }
    }

    private fun setupSortButton() {
        val sortOptions = arrayOf("Newest First", "Oldest First", "Highest Amount", "Lowest Amount")
        binding.btnSort.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sort Transactions")
                .setSingleChoiceItems(sortOptions, sortMode) { dialog, which ->
                    sortMode = which
                    binding.btnSort.text = "Sort: " + sortOptions[which].split(" ")[0]
                    applyFilterAndSort()
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun observeData() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            allList = transactions
            applyFilterAndSort()
        }
    }

    private fun applyFilterAndSort() {
        var filtered = allList

        if (filterType != "ALL") {
            filtered = filtered.filter { it.type == filterType }
        }

        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.categoryName.contains(searchQuery, ignoreCase = true) ||
                it.notes?.contains(searchQuery, ignoreCase = true) == true ||
                it.paymentMethod.contains(searchQuery, ignoreCase = true)
            }
        }

        filtered = when (sortMode) {
            1 -> filtered.sortedBy { it.date }
            2 -> filtered.sortedByDescending { it.amount }
            3 -> filtered.sortedBy { it.amount }
            else -> filtered.sortedByDescending { it.date }
        }

        binding.tvTransactionCount.text = "Showing ${filtered.size} transactions"
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
