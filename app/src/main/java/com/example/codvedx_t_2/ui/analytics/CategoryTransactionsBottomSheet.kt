package com.example.codvedx_t_2.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.databinding.BottomSheetCategoryTransactionsBinding
import com.example.codvedx_t_2.ui.transaction.AddEditTransactionBottomSheet
import com.example.codvedx_t_2.ui.transaction.TransactionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class CategoryTransactionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCategoryTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter
    private lateinit var userPreferences: UserPreferences

    private var categoryName: String = ""

    companion object {
        private const val ARG_CATEGORY_NAME = "arg_category_name"

        fun newInstance(categoryName: String): CategoryTransactionsBottomSheet {
            val fragment = CategoryTransactionsBottomSheet()
            val args = Bundle().apply {
                putString(ARG_CATEGORY_NAME, categoryName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryName = arguments?.getString(ARG_CATEGORY_NAME) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCategoryTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        adapter = TransactionAdapter(userPreferences.currencySymbol) { transaction ->
            val bottomSheet = AddEditTransactionBottomSheet.newInstance(transaction)
            bottomSheet.show(parentFragmentManager, "AddEditTransactionBottomSheet")
        }

        binding.rvCategoryTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryTransactions.adapter = adapter

        binding.tvCategorySheetTitle.text = "$categoryName History"

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val filtered = transactions.filter { it.categoryName == categoryName }
            val total = filtered.sumOf { it.amount }

            binding.tvCategorySheetTotal.text = "Total: ${userPreferences.currencySymbol}${String.format(Locale.getDefault(), "%.2f", total)}"
            binding.tvCategorySheetSubtitle.text = "Showing ${filtered.size} transactions in $categoryName"
            adapter.submitList(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
