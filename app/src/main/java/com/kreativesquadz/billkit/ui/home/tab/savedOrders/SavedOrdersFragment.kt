package com.kreativesquadz.billkit.ui.home.tab.savedOrders

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kreativesquadz.billkit.R

class SavedOrdersFragment : Fragment() {

    companion object {
        fun newInstance() = SavedOrdersFragment()
    }

    private val viewModel: SavedOrdersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_saved_orders, container, false)
    }
}