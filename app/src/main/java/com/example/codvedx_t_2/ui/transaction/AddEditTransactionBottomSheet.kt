package com.example.codvedx_t_2.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.codvedx_t_2.R
import com.example.codvedx_t_2.data.local.entity.AccountEntity
import com.example.codvedx_t_2.data.local.entity.CategoryEntity
import com.example.codvedx_t_2.data.local.entity.TransactionEntity
import com.example.codvedx_t_2.databinding.BottomSheetAddEditTransactionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditTransactionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddEditTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels()

    private var selectedType = "EXPENSE"
    private var selectedDateMs: Long = System.currentTimeMillis()
    private val calendar = Calendar.getInstance()

    private var categoriesList = listOf<CategoryEntity>()
    private var accountsList = listOf<AccountEntity>()

    private var selectedCategory: CategoryEntity? = null
    private var selectedAccount: AccountEntity? = null

    var existingTransaction: TransactionEntity? = null

    companion object {
        fun newInstance(transaction: TransactionEntity? = null): AddEditTransactionBottomSheet {
            val sheet = AddEditTransactionBottomSheet()
            sheet.existingTransaction = transaction
            return sheet
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTypeToggle()
        setupDatePicker()
        setupPaymentMethods()
        observeData()

        existingTransaction?.let { tx ->
            populateExistingData(tx)
        }

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }

        binding.btnDelete.setOnClickListener {
            existingTransaction?.let { tx ->
                viewModel.deleteTransaction(tx)
                dismiss()
            }
        }
    }

    private fun populateExistingData(tx: TransactionEntity) {
        binding.tvBottomSheetTitle.text = "Edit Transaction"
        binding.btnDelete.visibility = View.VISIBLE

        selectedType = tx.type
        if (tx.type == "INCOME") {
            binding.toggleType.check(R.id.btn_type_income)
        } else {
            binding.toggleType.check(R.id.btn_type_expense)
        }

        binding.etAmount.setText(tx.amount.toString())
        binding.etTitle.setText(tx.title)
        binding.etNotes.setText(tx.notes ?: "")
        binding.etTags.setText(tx.tags ?: "")
        binding.cbRecurring.isChecked = tx.isRecurring
        binding.spinnerPaymentMethod.setText(tx.paymentMethod, false)

        selectedDateMs = tx.date
        calendar.timeInMillis = selectedDateMs
        updateDateButtonText()
    }

    private fun setupTypeToggle() {
        binding.toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedType = if (checkedId == R.id.btn_type_income) "INCOME" else "EXPENSE"
                updateCategoryDropdown()
            }
        }
    }

    private fun setupDatePicker() {
        updateDateButtonText()
        binding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateMs = calendar.timeInMillis
                    updateDateButtonText()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateButtonText() {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.btnSelectDate.text = "Date: " + sdf.format(Date(selectedDateMs))
    }

    private fun setupPaymentMethods() {
        val paymentMethods = arrayOf("Cash", "Bank account", "Debit card", "Credit card", "UPI", "Wallet", "Net banking", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, paymentMethods)
        binding.spinnerPaymentMethod.setAdapter(adapter)
        binding.spinnerPaymentMethod.setText(paymentMethods[0], false)
    }

    private fun observeData() {
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            categoriesList = categories
            updateCategoryDropdown()
        }

        viewModel.allAccounts.observe(viewLifecycleOwner) { accounts ->
            accountsList = accounts
            val names = accounts.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            binding.spinnerAccount.setAdapter(adapter)

            if (accounts.isNotEmpty()) {
                val match = accounts.firstOrNull { it.id == existingTransaction?.accountId } ?: accounts[0]
                selectedAccount = match
                binding.spinnerAccount.setText(match.name, false)
            }
        }

        binding.spinnerAccount.setOnItemClickListener { _, _, position, _ ->
            if (position in accountsList.indices) {
                selectedAccount = accountsList[position]
            }
        }
    }

    private fun updateCategoryDropdown() {
        val filtered = categoriesList.filter { it.type == selectedType }
        val names = filtered.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        binding.spinnerCategory.setAdapter(adapter)

        if (filtered.isNotEmpty()) {
            val match = filtered.firstOrNull { it.id == existingTransaction?.categoryId } ?: filtered[0]
            selectedCategory = match
            binding.spinnerCategory.setText(match.name, false)
        }

        binding.spinnerCategory.setOnItemClickListener { _, _, position, _ ->
            if (position in filtered.indices) {
                selectedCategory = filtered[position]
            }
        }
    }

    private fun saveTransaction() {
        val amountStr = binding.etAmount.text.toString().trim()
        val title = binding.etTitle.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()
        val tags = binding.etTags.text.toString().trim()
        val paymentMethod = binding.spinnerPaymentMethod.text.toString()

        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val category = selectedCategory
        val account = selectedAccount

        if (category == null || account == null) {
            Toast.makeText(requireContext(), "Please select a category and account", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionTitle = if (title.isEmpty()) category.name else title

        viewModel.saveTransaction(
            id = existingTransaction?.id ?: 0,
            type = selectedType,
            title = transactionTitle,
            amount = amount,
            categoryId = category.id,
            categoryName = category.name,
            categoryIcon = category.iconName,
            categoryColor = category.colorHex,
            accountId = account.id,
            accountName = account.name,
            paymentMethod = paymentMethod,
            date = selectedDateMs,
            notes = if (notes.isEmpty()) null else notes,
            tags = if (tags.isEmpty()) null else tags,
            isRecurring = binding.cbRecurring.isChecked,
            oldTransaction = existingTransaction
        )

        Toast.makeText(requireContext(), "Transaction saved", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
