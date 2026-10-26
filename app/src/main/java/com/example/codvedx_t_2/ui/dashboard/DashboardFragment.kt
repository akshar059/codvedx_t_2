package com.example.codvedx_t_2.ui.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codvedx_t_2.R
import com.example.codvedx_t_2.databinding.FragmentDashboardBinding
import com.example.codvedx_t_2.ui.transaction.AddEditTransactionBottomSheet
import com.example.codvedx_t_2.ui.transaction.TransactionAdapter
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        val symbol = viewModel.userPreferences.currencySymbol
        adapter = TransactionAdapter(symbol) { transaction ->
            val bottomSheet = AddEditTransactionBottomSheet.newInstance(transaction)
            bottomSheet.show(parentFragmentManager, "AddEditTransactionBottomSheet")
        }

        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentTransactions.adapter = adapter

        updateProfileUI()

        binding.ivUserAvatar.setOnClickListener {
            openUserProfileSheet()
        }

        binding.tvUserName.setOnClickListener {
            openUserProfileSheet()
        }

        binding.tvGreeting.setOnClickListener {
            openUserProfileSheet()
        }

        binding.tvViewAllTransactions.setOnClickListener {
            findNavController().navigate(R.id.navigation_transactions)
        }

        observeData(symbol)
    }

    private fun updateProfileUI() {
        binding.tvUserName.text = viewModel.userPreferences.userName

        val imageUriStr = viewModel.userPreferences.profileImageUri
        if (!imageUriStr.isNullOrEmpty()) {
            try {
                binding.ivUserAvatar.setImageURI(Uri.parse(imageUriStr))
                binding.ivUserAvatar.setPadding(0, 0, 0, 0)
            } catch (e: Exception) {
                binding.ivUserAvatar.setImageResource(R.drawable.ic_person)
            }
        } else {
            binding.ivUserAvatar.setImageResource(R.drawable.ic_person)
        }
    }

    private fun openUserProfileSheet() {
        val profileSheet = UserProfileBottomSheet {
            val newSymbol = viewModel.userPreferences.currencySymbol
            updateProfileUI()
            observeData(newSymbol)
        }
        profileSheet.show(parentFragmentManager, "UserProfileBottomSheet")
    }

    private fun observeData(symbol: String) {
        viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
            binding.tvTotalBalance.text = "$symbol ${String.format(Locale.getDefault(), "%.2f", balance ?: 0.0)}"
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvTotalIncome.text = "$symbol ${String.format(Locale.getDefault(), "%.2f", income ?: 0.0)}"
            updateSavings(symbol)
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            val totalExpense = expense ?: 0.0
            binding.tvTotalExpense.text = "$symbol ${String.format(Locale.getDefault(), "%.2f", totalExpense)}"
            updateSavings(symbol)

            // Budget Progress
            val monthlyBudget = viewModel.userPreferences.monthlyBudget
            val pct = if (monthlyBudget > 0) ((totalExpense / monthlyBudget) * 100).toInt() else 0
            binding.progressBudget.progress = pct.coerceAtMost(100)
            binding.tvBudgetPercentage.text = "$pct%"
            binding.tvBudgetSpent.text = "Spent: $symbol${String.format(Locale.getDefault(), "%.0f", totalExpense)}"
            val remaining = (monthlyBudget - totalExpense).coerceAtLeast(0.0)
            binding.tvBudgetRemaining.text = "Remaining: $symbol${String.format(Locale.getDefault(), "%.0f", remaining)}"

            if (pct >= 90) {
                binding.tvBudgetWarning.visibility = View.VISIBLE
            } else {
                binding.tvBudgetWarning.visibility = View.GONE
            }
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val recentList = transactions.take(5)
            adapter.submitList(recentList)
        }
    }

    private fun updateSavings(symbol: String) {
        val inc = viewModel.totalIncome.value ?: 0.0
        val exp = viewModel.totalExpense.value ?: 0.0
        val savings = inc - exp
        binding.tvNetSavings.text = "$symbol ${String.format(Locale.getDefault(), "%.2f", savings)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
