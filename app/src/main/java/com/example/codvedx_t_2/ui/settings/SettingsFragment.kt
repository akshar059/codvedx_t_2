package com.example.codvedx_t_2.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.codvedx_t_2.R
import com.example.codvedx_t_2.data.local.AppDatabase
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.data.repository.ExpenseRepository
import com.example.codvedx_t_2.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreferences: UserPreferences
    private lateinit var repository: ExpenseRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        userPreferences = UserPreferences(requireContext())
        repository = ExpenseRepository(AppDatabase.getInstance(requireContext()))

        setupCurrencySelection()
        setupDarkModeSwitch()

        binding.btnExportCsv.setOnClickListener {
            exportTransactionsToCsv()
        }
    }

    private fun setupCurrencySelection() {
        when (userPreferences.currencySymbol) {
            "$" -> binding.chipUsdSettings.isChecked = true
            "€" -> binding.chipEurSettings.isChecked = true
            "£" -> binding.chipGbpSettings.isChecked = true
            else -> binding.chipInrSettings.isChecked = true
        }

        binding.chipGroupCurrencySettings.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.contains(R.id.chip_usd_settings)) {
                userPreferences.currencySymbol = "$"
            } else if (checkedIds.contains(R.id.chip_eur_settings)) {
                userPreferences.currencySymbol = "€"
            } else if (checkedIds.contains(R.id.chip_gbp_settings)) {
                userPreferences.currencySymbol = "£"
            } else {
                userPreferences.currencySymbol = "₹"
            }
            Toast.makeText(requireContext(), "Currency updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDarkModeSwitch() {
        binding.switchDarkMode.isChecked = userPreferences.isDarkMode
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            userPreferences.isDarkMode = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun exportTransactionsToCsv() {
        lifecycleScope.launch {
            val transactions = repository.allTransactions.first()
            if (transactions.isEmpty()) {
                Toast.makeText(requireContext(), "No transactions to export", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val csvHeader = "ID,Type,Title,Amount,Category,PaymentMethod,Account,Date,Notes,Tags\n"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            val csvData = StringBuilder(csvHeader)
            transactions.forEach { tx ->
                val dateStr = sdf.format(Date(tx.date))
                csvData.append("${tx.id},${tx.type},\"${tx.title}\",${tx.amount},\"${tx.categoryName}\",\"${tx.paymentMethod}\",\"${tx.accountName}\",\"$dateStr\",\"${tx.notes ?: ""}\",\"${tx.tags ?: ""}\"\n")
            }

            try {
                val file = File(requireContext().cacheDir, "Expense_Report.csv")
                val fos = FileOutputStream(file)
                fos.write(csvData.toString().toByteArray())
                fos.close()

                val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share CSV Report"))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
