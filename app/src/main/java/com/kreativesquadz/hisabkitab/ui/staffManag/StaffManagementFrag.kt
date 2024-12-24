package com.kreativesquadz.hisabkitab.ui.staffManag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.hisabkitab.BR
import com.kreativesquadz.hisabkitab.R
import com.kreativesquadz.hisabkitab.adapter.GenericAdapter
import com.kreativesquadz.hisabkitab.databinding.FragmentStaffManagementBinding
import com.kreativesquadz.hisabkitab.interfaces.OnItemClickListener
import com.kreativesquadz.hisabkitab.model.Staff
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StaffManagementFrag : Fragment() {
    var _binding: FragmentStaffManagementBinding? = null
    val binding get() = _binding!!
    private val viewModel: StaffManagementViewModel by viewModels()
    private lateinit var adapter: GenericAdapter<Staff>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getStaffList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffManagementBinding.inflate(inflater, container, false)
        observers()
        setupRecyclerView()
        onClickListeners()
        return binding.root
    }

    private fun observers() {
        viewModel.staffList.observe(viewLifecycleOwner) {
            it.data?.let {
                adapter.submitList(it)
            }
        }

    }
    private fun onClickListeners() {
        binding.btnAddStaff.setOnClickListener {
            findNavController().navigate(R.id.action_staffManagementFrag_to_addStaffFragment)
        }

    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.staffList.value?.data ?: emptyList(),
            object : OnItemClickListener<Staff> {
                override fun onItemClick(item: Staff) {
                    val action = StaffManagementFragDirections.actionStaffManagementFragToStaffDetailsFragment(item)
                    findNavController().navigate(action)
                }
            },
            R.layout.item_staff,
            BR.staff // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
}