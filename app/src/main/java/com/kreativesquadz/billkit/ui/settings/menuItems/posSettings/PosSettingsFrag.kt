package com.kreativesquadz.billkit.ui.settings.menuItems.posSettings

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.databinding.FragmentPosSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PosSettingsFrag : Fragment() {
    private var _binding: FragmentPosSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PosSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPosSettingsBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        return binding.root
    }

    private fun observers() {
    }
    private fun onClickListeners() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}