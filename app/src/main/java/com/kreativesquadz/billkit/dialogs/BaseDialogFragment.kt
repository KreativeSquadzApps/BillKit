package com.example.customdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment<VB : ViewDataBinding> : DialogFragment() {

    protected lateinit var binding: VB
        private set

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflateBinding(inflater, container)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
    // Abstract functions to be implemented by subclasses
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB
}
