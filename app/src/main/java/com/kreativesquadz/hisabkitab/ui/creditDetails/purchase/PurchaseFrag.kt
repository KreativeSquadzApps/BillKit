package com.kreativesquadz.hisabkitab.ui.creditDetails.purchase

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kreativesquadz.hisabkitab.R

class PurchaseFrag : Fragment() {

    companion object {
        fun newInstance() = PurchaseFrag()
    }

    private val viewModel: PurchaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_purchase, container, false)
    }
}