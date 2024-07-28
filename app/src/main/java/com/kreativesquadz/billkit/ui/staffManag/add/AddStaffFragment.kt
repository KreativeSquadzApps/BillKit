package com.kreativesquadz.billkit.ui.staffManag.add

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
import com.kreativesquadz.billkit.databinding.FragmentAddStaffBinding
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.model.Staff
import com.kreativesquadz.billkit.model.UserSetting
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddStaffFragment : Fragment() {
    var _binding: FragmentAddStaffBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddStaffViewModel by viewModels()
    val roleList = listOf("Administrator",
        "Cashier",
        "Sales")
    var role = ""
    var permissions : MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getStaffList()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStaffBinding.inflate(inflater, container, false)
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
        binding.switchBillHistory.isSelected = false
        binding.switchInventory.isSelected = false
        binding.switchCustomer.isSelected = false
        binding.switchCreditNote.isSelected = false
        binding.switchCredit.isSelected = false
        binding.switchDaybook.isSelected = false
        binding.switchShopDetails.isSelected = false

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
        return Staff(
            id = 0,
            adminId = Config.userId,
            name = binding.etName.text.toString(),
            mailId = binding.etEmail.text.toString(),
            password = binding.etPassword.text.toString(),
            status = "Active",
            role = role,
            totalSalesCount = 0,
            permissions = permissions.joinToString ("," ),
            isSynced = 0)
    }

    private fun setupSpinnerRole(itemList: List<String>) {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownRole.setAdapter(adapterStockUnit)
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