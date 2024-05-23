package com.kreativesquadz.billkit.ui.home.tab.quickSale

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.kreativesquadz.billkit.databinding.FragmentQuickSaleBinding
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuickSaleFragment : Fragment() {

    private var _binding: FragmentQuickSaleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuickSaleViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickSaleBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.sharedViewModels = sharedViewModel


        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey("object") }?.apply {
        }

    }



}