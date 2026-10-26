package com.example.codvedx_t_2.ui.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.codvedx_t_2.R
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.databinding.BottomSheetUserProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UserProfileBottomSheet(
    private val onProfileUpdated: () -> Unit = {}
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreferences: UserPreferences
    private var selectedImageUriStr: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                // Grant persistable URI permission if needed
                requireContext().contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not a document URI
            }

            selectedImageUriStr = it.toString()
            binding.ivProfilePreview.setImageURI(it)
            binding.ivProfilePreview.setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        selectedImageUriStr = userPreferences.profileImageUri

        // Pre-fill profile data
        binding.etProfileName.setText(userPreferences.userName)
        binding.etProfileBudget.setText(userPreferences.monthlyBudget.toInt().toString())

        val existingUri = userPreferences.profileImageUri
        if (!existingUri.isNullOrEmpty()) {
            try {
                binding.ivProfilePreview.setImageURI(Uri.parse(existingUri))
                binding.ivProfilePreview.setPadding(0, 0, 0, 0)
            } catch (e: Exception) {
                binding.ivProfilePreview.setImageResource(R.drawable.ic_person)
            }
        }

        binding.ivProfilePreview.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        when (userPreferences.currencySymbol) {
            "$" -> binding.chipProfileUsd.isChecked = true
            "€" -> binding.chipProfileEur.isChecked = true
            "£" -> binding.chipProfileGbp.isChecked = true
            else -> binding.chipProfileInr.isChecked = true
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etProfileName.text.toString().trim()
            val budgetStr = binding.etProfileBudget.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var selectedCurrency = "₹"
            val checkedChipId = binding.chipGroupProfileCurrency.checkedChipId
            when (checkedChipId) {
                R.id.chip_profile_usd -> selectedCurrency = "$"
                R.id.chip_profile_eur -> selectedCurrency = "€"
                R.id.chip_profile_gbp -> selectedCurrency = "£"
                else -> selectedCurrency = "₹"
            }

            userPreferences.userName = name
            userPreferences.currencySymbol = selectedCurrency
            userPreferences.monthlyBudget = budgetStr.toDoubleOrNull() ?: userPreferences.monthlyBudget
            userPreferences.profileImageUri = selectedImageUriStr

            Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
            onProfileUpdated()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
