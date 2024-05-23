package com.kreativesquadz.billkit.ui.staffManag

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kreativesquadz.billkit.R

class StaffManagementFrag : Fragment() {

    companion object {
        fun newInstance() = StaffManagementFrag()
    }

    private val viewModel: StaffManagementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_staff_management, container, false)
    }
}