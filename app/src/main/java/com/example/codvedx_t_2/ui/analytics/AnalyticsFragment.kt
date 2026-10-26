package com.example.codvedx_t_2.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.codvedx_t_2.data.local.entity.TransactionEntity
import com.example.codvedx_t_2.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            setupInsights(transactions)
            setupPieChart(transactions)
            setupBarChart(transactions)
        }
    }

    private fun setupInsights(transactions: List<TransactionEntity>) {
        val expenses = transactions.filter { it.type == "EXPENSE" }
        if (expenses.isEmpty()) {
            binding.tvTopCategory.text = "N/A"
            binding.tvAvgDaily.text = "${viewModel.userPreferences.currencySymbol} 0"
            return
        }

        val categoryGroup = expenses.groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val topCategory = categoryGroup.maxByOrNull { it.value }
        binding.tvTopCategory.text = topCategory?.key ?: "N/A"

        val totalExp = expenses.sumOf { it.amount }
        val avgDaily = totalExp / 30.0
        binding.tvAvgDaily.text = "${viewModel.userPreferences.currencySymbol} ${String.format(Locale.getDefault(), "%.0f", avgDaily)}"
    }

    private fun setupPieChart(transactions: List<TransactionEntity>) {
        val expenses = transactions.filter { it.type == "EXPENSE" }
        val categoryGroup = expenses.groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        categoryGroup.forEach { (catName, amount) ->
            entries.add(PieEntry(amount.toFloat(), catName))
            val tx = expenses.firstOrNull { it.categoryName == catName }
            val c = if (tx != null && tx.categoryColor != 0) tx.categoryColor else Color.parseColor("#4F46E5")
            colors.add(c)
        }

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No Expenses"))
            colors.add(Color.LTGRAY)
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextColor = Color.WHITE
            valueTextSize = 13f
            sliceSpace = 3f
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            centerText = "Expenses"
            setCenterTextSize(16f)
            setHoleColor(Color.TRANSPARENT)
            legend.isEnabled = true
            legend.textSize = 12f
            animateY(1000)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        val categoryName = e.label
                        if (categoryName.isNotEmpty() && categoryName != "No Expenses") {
                            val bottomSheet = CategoryTransactionsBottomSheet.newInstance(categoryName)
                            bottomSheet.show(parentFragmentManager, "CategoryTransactionsBottomSheet")
                        }
                    }
                }

                override fun onNothingSelected() {}
            })

            invalidate()
        }
    }

    private fun setupBarChart(transactions: List<TransactionEntity>) {
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }.toFloat()
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }.toFloat()

        val entriesIncome = arrayListOf(BarEntry(0f, totalIncome))
        val entriesExpense = arrayListOf(BarEntry(1f, totalExpense))

        val setIncome = BarDataSet(entriesIncome, "Income").apply {
            color = Color.parseColor("#10B981")
            valueTextSize = 12f
        }

        val setExpense = BarDataSet(entriesExpense, "Expense").apply {
            color = Color.parseColor("#EF4444")
            valueTextSize = 12f
        }

        binding.barChart.apply {
            data = BarData(setIncome, setExpense)
            description.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(arrayOf("Income", "Expenses"))
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = false
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
