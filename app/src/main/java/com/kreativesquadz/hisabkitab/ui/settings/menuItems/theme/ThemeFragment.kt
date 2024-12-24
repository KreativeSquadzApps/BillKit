package com.kreativesquadz.hisabkitab.ui.settings.menuItems.theme

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.databinding.FragmentThemeBinding

class ThemeFragment : Fragment() {
    private var _binding: FragmentThemeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeBinding.inflate(inflater, container, false)
        binding.darkThemeLayout.setOnClickListener {
            setNightMode(true)
            binding.darkThemeLayout.setBackgroundResource(R.drawable.selected_corner_four_stroke)
            binding.lightThemeLayout.setBackgroundResource(R.drawable.unselected_corner_four_stroke)
        }
        binding.lightThemeLayout.setOnClickListener {
            setNightMode(false)
            binding.lightThemeLayout.setBackgroundResource(R.drawable.selected_corner_four_stroke)
            binding.darkThemeLayout.setBackgroundResource(R.drawable.unselected_corner_four_stroke)
        }

        return  binding.root
    }
    fun setNightMode(isNightMode: Boolean) {
        val mode = if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        // Save preference
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("night_mode", isNightMode).apply()
    }
}