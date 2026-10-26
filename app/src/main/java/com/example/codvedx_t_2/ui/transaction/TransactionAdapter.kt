package com.example.codvedx_t_2.ui.transaction

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.codvedx_t_2.data.local.entity.TransactionEntity
import com.example.codvedx_t_2.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val currencySymbol: String = "₹",
    private val onItemClick: (TransactionEntity) -> Unit = {}
) : ListAdapter<TransactionEntity, TransactionAdapter.TransactionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionEntity) {
            binding.tvTitle.text = item.title

            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date(item.date))
            binding.tvSubtitle.text = "${item.categoryName} • ${item.paymentMethod} • $dateStr"

            val circleDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(if (item.categoryColor != 0) item.categoryColor else Color.parseColor("#3F51B5"))
            }
            binding.iconContainer.background = circleDrawable

            if (item.type == "INCOME") {
                binding.tvAmount.text = "+ $currencySymbol${String.format(Locale.getDefault(), "%.2f", item.amount)}"
                binding.tvAmount.setTextColor(Color.parseColor("#2E7D32"))
            } else {
                binding.tvAmount.text = "- $currencySymbol${String.format(Locale.getDefault(), "%.2f", item.amount)}"
                binding.tvAmount.setTextColor(Color.parseColor("#D32F2F"))
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<TransactionEntity>() {
        override fun areItemsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TransactionEntity, newItem: TransactionEntity): Boolean =
            oldItem == newItem
    }
}
