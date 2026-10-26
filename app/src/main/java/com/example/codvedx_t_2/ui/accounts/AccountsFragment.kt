package com.example.codvedx_t_2.ui.accounts

import android.graphics.Color
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
import com.example.codvedx_t_2.data.local.entity.AccountEntity
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.data.repository.ExpenseRepository
import com.example.codvedx_t_2.databinding.FragmentAccountsBinding
import com.example.codvedx_t_2.databinding.ItemAccountBinding
import kotlinx.coroutines.launch
import java.util.Locale

class AccountsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ExpenseRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var adapter: AccountsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        repository = ExpenseRepository(AppDatabase.getInstance(requireContext()))
        userPreferences = UserPreferences(requireContext())

        adapter = AccountsAdapter(userPreferences.currencySymbol) { account ->
            showEditAccountDialog(account)
        }
        binding.rvAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAccounts.adapter = adapter

        binding.btnAddAccount.setOnClickListener {
            showAddAccountDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allAccounts.collect { accounts ->
                adapter.submitList(accounts)
            }
        }
    }

    private fun showAddAccountDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etName = EditText(requireContext()).apply { hint = "Account Name (e.g. Axis Bank)" }
        val etType = EditText(requireContext()).apply { hint = "Type (Bank / Cash / Card)" }
        val etBalance = EditText(requireContext()).apply { hint = "Opening Balance" }

        layout.addView(etName)
        layout.addView(etType)
        layout.addView(etBalance)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Account")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val type = etType.text.toString().trim()
                val bal = etBalance.text.toString().toDoubleOrNull() ?: 0.0

                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        repository.insertAccount(
                            AccountEntity(
                                name = name,
                                type = if (type.isEmpty()) "Bank" else type,
                                balance = bal,
                                initialBalance = bal,
                                colorHex = Color.parseColor("#1565C0")
                            )
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Account Name is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditAccountDialog(account: AccountEntity) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etName = EditText(requireContext()).apply {
            hint = "Account Name"
            setText(account.name)
        }
        val etType = EditText(requireContext()).apply {
            hint = "Type (Bank / Cash / Card)"
            setText(account.type)
        }
        val etBalance = EditText(requireContext()).apply {
            hint = "Current Balance"
            setText(account.balance.toString())
        }

        layout.addView(etName)
        layout.addView(etType)
        layout.addView(etBalance)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Account")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val name = etName.text.toString().trim()
                val type = etType.text.toString().trim()
                val bal = etBalance.text.toString().toDoubleOrNull() ?: 0.0

                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        repository.updateAccount(
                            account.copy(
                                name = name,
                                type = if (type.isEmpty()) "Bank" else type,
                                balance = bal
                            )
                        )
                    }
                } else {
                    Toast.makeText(requireContext(), "Account Name is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteAccount(account)
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

class AccountsAdapter(
    private val currencySymbol: String,
    private val onItemClick: (AccountEntity) -> Unit = {}
) : RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {
    private var items = listOf<AccountEntity>()

    fun submitList(list: List<AccountEntity>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AccountEntity) {
            binding.tvAccountName.text = item.name
            binding.tvAccountType.text = item.type
            binding.tvAccountBalance.text = "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", item.balance)}"

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
