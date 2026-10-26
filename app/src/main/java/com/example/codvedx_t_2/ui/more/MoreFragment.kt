package com.example.codvedx_t_2.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.codvedx_t_2.R
import com.example.codvedx_t_2.databinding.FragmentMoreBinding

class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardGoals.setOnClickListener {
            findNavController().navigate(R.id.navigation_goals)
        }

        binding.cardAccounts.setOnClickListener {
            findNavController().navigate(R.id.navigation_accounts)
        }

        binding.cardSubscriptions.setOnClickListener {
            findNavController().navigate(R.id.navigation_subscriptions)
        }

        binding.cardSettings.setOnClickListener {
            findNavController().navigate(R.id.navigation_settings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
