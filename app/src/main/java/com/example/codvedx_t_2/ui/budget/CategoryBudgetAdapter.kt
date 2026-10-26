package com.example.codvedx_t_2.ui.budget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codvedx_t_2.data.local.entity.CategoryEntity
import com.example.codvedx_t_2.databinding.ItemCategoryBudgetBinding
import java.util.Locale

data class CategoryBudgetUiModel(
    val category: CategoryEntity,
    val spent: Double
)

class CategoryBudgetAdapter(
    private val currencySymbol: String = "₹",
    private val onItemClick: (CategoryBudgetUiModel) -> Unit = {}
) : RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder>() {

    private var items = listOf<CategoryBudgetUiModel>()

    fun submitList(list: List<CategoryBudgetUiModel>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemCategoryBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: CategoryBudgetUiModel) {
            val cat = model.category
            binding.tvCategoryName.text = cat.name

            val budget = cat.budget
            val spent = model.spent
            val budgetText = if (budget > 0) {
                "$currencySymbol${String.format(Locale.getDefault(), "%.0f", spent)} / $currencySymbol${String.format(Locale.getDefault(), "%.0f", budget)}"
            } else {
                "$currencySymbol${String.format(Locale.getDefault(), "%.0f", spent)} spent (Tap to set budget)"
            }
            binding.tvCategoryAmount.text = budgetText

            val progressPct = if (budget > 0) ((spent / budget) * 100).toInt() else 0
            binding.progressCategory.progress = progressPct.coerceAtMost(100)

            binding.root.setOnClickListener { onItemClick(model) }
        }
    }
}
