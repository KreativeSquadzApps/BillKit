package com.kreativesquadz.billkit.ui.staffManag.edit

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericSpinnerAdapter
import com.kreativesquadz.billkit.databinding.FragmentEditStaffBinding
import com.kreativesquadz.billkit.model.Staff
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditStaffFragment : Fragment() {
    private var _binding: FragmentEditStaffBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditStaffViewModel by viewModels()
    val roleList = listOf("Administrator",
        "Cashier",
        "Sales")
    var role = ""
    var permissions : MutableList<String> = ArrayList()

    val staffDetails by lazy {
        arguments?.getSerializable("staff") as? Staff
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditStaffBinding.inflate(inflater,container, false)
        binding.staffDetails = staffDetails
        permissions()
        observers()
        onClickListeners()
        setupSpinnerRole(roleList)
        return binding.root
    }

    private fun observers() {
        viewModel.staffStatus.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            if (it.invoiceId == 200){
                findNavController().popBackStack()
            }
        }
    }

    private fun onClickListeners() {
        binding.btnAdd.setOnClickListener {
            val staff = getStaff()
            viewModel.addStaffObj(requireContext(),staff)
        }
    }

    private fun permissions(){
        staffDetails?.permissions?.let {
            it.split(",").forEach { permission ->
                permissions.add(permission)
                when (permission) {
                    resources.getString(R.string.menu_bill_history) -> binding.switchBillHistory.isChecked = true
                    resources.getString(R.string.menu_inventory) -> binding.switchInventory.isChecked = true
                    resources.getString(R.string.menu_customer_management) -> binding.switchCustomer.isChecked = true
                    resources.getString(R.string.menu_credit_notes) -> binding.switchCreditNote.isChecked = true
                    resources.getString(R.string.menu_credit_details) -> binding.switchCredit.isChecked = true
                    resources.getString(R.string.menu_dayBook) -> binding.switchDaybook.isChecked = true
                    resources.getString(R.string.menu_settings_shop_details) -> binding.switchShopDetails.isChecked = true
                    else -> {}
                }
            }
        } ?: run {
            // Handle the case where permissions is null
            // Maybe log an error or set default switch states
        }
        binding.switchBillHistory.isSelected = true
        binding.switchBillHistory.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                permissions.add(resources.getString(R.string.menu_bill_history))
            } else {
                permissions.remove(resources.getString(R.string.menu_bill_history))
            }

            Toast.makeText(requireContext(), permissions.toString(), Toast.LENGTH_SHORT).show()
        }
        binding.switchInventory.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                permissions.add(resources.getString(R.string.menu_inventory))
            } else {
                permissions.remove(resources.getString(R.string.menu_inventory))
            }
        }
        binding.switchCustomer.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                permissions.add(resources.getString(R.string.menu_customer_management))
            } else {
                permissions.remove(resources.getString(R.string.menu_customer_management))
            }
        }
        binding.switchCreditNote.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                permissions.add(resources.getString(R.string.menu_credit_notes))
            } else {
                permissions.remove(resources.getString(R.string.menu_credit_notes))
            }
        }
        binding.switchCredit.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                permissions.add(resources.getString(R.string.menu_credit_details))
            } else {
                permissions.remove(resources.getString(R.string.menu_credit_details))
            }
        }
        binding.switchDaybook.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                permissions.add(resources.getString(R.string.menu_dayBook))
            } else {
                permissions.remove(resources.getString(R.string.menu_dayBook))
            }
        }
        binding.switchShopDetails.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                permissions.add(resources.getString(R.string.menu_settings_shop_details))
            } else {
                permissions.remove(resources.getString(R.string.menu_settings_shop_details))
            }
        }

    }

    private fun getStaff(): Staff {
        if (role.isEmpty()){
            role = binding.dropdownRole.text.toString()
        }
        return Staff(
            id = staffDetails?.id ?: 0,
            adminId = Config.userId,
            name = binding.etName.text.toString(),
            mailId = binding.etEmail.text.toString(),
            password = binding.etPassword.text.toString(),
            status = staffDetails?.status ?: "Active",
            role = role,
            totalSalesCount = staffDetails?.totalSalesCount ?: 0,
            totalSalesAmount = staffDetails?.totalSalesAmount ?: 0.0,
            permissions = permissions.joinToString ("," ),
            isSynced = staffDetails?.isSynced ?: 0)
    }

    private fun setupSpinnerRole(itemList: List<String>) {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownRole.setAdapter(adapterStockUnit)
        binding.dropdownRole.setText(role, false)
        binding.dropdownRole.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            role = selectedItem!!
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}