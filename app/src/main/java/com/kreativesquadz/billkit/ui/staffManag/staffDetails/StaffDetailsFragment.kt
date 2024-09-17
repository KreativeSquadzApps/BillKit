package com.kreativesquadz.billkit.ui.staffManag.staffDetails

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.adapter.showCustomAlertDialog
import com.kreativesquadz.billkit.databinding.FragmentStaffDetailsBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.DialogData
import com.kreativesquadz.billkit.model.Staff
import com.kreativesquadz.billkit.ui.staffManag.edit.EditStaffViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class StaffDetailsFragment : Fragment() {
    var _binding: FragmentStaffDetailsBinding? = null
    private val viewModel: StaffDetailsViewModel by viewModels()
    private val editStaffViewModel: EditStaffViewModel by viewModels()
    val binding get() = _binding!!
    var staffId by Delegates.notNull<Long>()
    private lateinit var adapter: GenericAdapter<String>

    val staff by lazy {
        arguments?.getSerializable("staff") as? Staff
    }

    override fun onResume() {
        super.onResume()
        staffId = staff!!.id
        viewModel.getStaffDetails(staffId)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffDetailsBinding.inflate(inflater, container, false)
        observers()
        onClickListeners()
        setupRecyclerView()
        return binding.root
    }


    private fun onClickListeners() {
        binding.viewSales.setOnClickListener {
            staff?.let {
                val action = StaffDetailsFragmentDirections.actionStaffDetailsFragmentToBillHistoryFrag("Created By "+it.name)
                findNavController().navigate(action)
            }

        }
        binding.btnEdit.setOnClickListener {
            staff?.let {
                val action = StaffDetailsFragmentDirections.actionStaffDetailsFragmentToEditStaffFragment(it)
                findNavController().navigate(action)
            }
        }
        binding.btnActivate.setOnClickListener {
            staff?.let {
                if(it.status == "Active"){
                    setupPopup(it.name ,binding.staffStatusr.text.toString()){
                        editStaffViewModel.addStaffObj(requireContext(),it.copy(status = "Deactivated"))
                    }
                }else{
                    setupPopup(it.name ,binding.staffStatusr.text.toString()){
                        editStaffViewModel.addStaffObj(requireContext(),it.copy(status = "Active"))
                    }
                }
            }
        }

        binding.btnDelete.setOnClickListener {
            staff?.let {
                setupPopup(it.name ,"Delete"){
                    viewModel.deleteStaff(requireContext(),it.id)
                    findNavController().popBackStack()
                }
            }
        }

    }
    private fun setupPopup(name : String,isActivate : String ,action: () -> Unit){
        val dialogData: DialogData
        if(isActivate == "Active"){
            dialogData = DialogData(
                title = "Activate Staff",
                info = "Are you sure you want to Activate ${name} Staff ?",
                positiveButtonText = "Activate",
                negativeButtonText = "Cancel"
            )
        }else if(isActivate == "Delete"){
            dialogData = DialogData(
                title = "Delete Staff",
                info = "Are you sure you want to Delete ${name} Staff ?",
                positiveButtonText = "Delete",
                negativeButtonText = "Cancel"
            )
        } else{
            dialogData = DialogData(
                title = "DeActivate Staff",
                info = "Are you sure you want to DeActivate ${name} Staff ?",
                positiveButtonText = "Deactivate",
                negativeButtonText = "Cancel"
            )
        }

        showCustomAlertDialog(
            context = requireActivity(),
            dialogData = dialogData,
            positiveAction = {
                action()
            },
            negativeAction = {
                // Handle negative button action
                // E.g., dismiss the dialog
            }
        )
    }


    private fun observers() {
        editStaffViewModel.staffStatus.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), "Staff Status Updated", Toast.LENGTH_SHORT).show()
            if (it.invoiceId == 200){
                staffId = staff!!.id
                viewModel.getStaffDetails(staffId)
            }
        }
        viewModel.staffDetails.observe(viewLifecycleOwner) {
            binding.staff = it
            adapter.submitList(it.permissions.split(","))
        }
    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            staff?.permissions?.split(",") ?: emptyList(),
            object : OnItemClickListener<String> {
                override fun onItemClick(item: String) {

                }
            },
            R.layout.item_permissions,
            BR.permission // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}