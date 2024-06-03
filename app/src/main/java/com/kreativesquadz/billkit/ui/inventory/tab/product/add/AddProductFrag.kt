package com.kreativesquadz.billkit.ui.inventory.tab.product.add

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import com.kreativesquadz.billkit.BR
import android.view.ViewGroup
import android.widget.Toast
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericSpinnerAdapter
import com.kreativesquadz.billkit.databinding.FragmentAddProductBinding
import com.kreativesquadz.billkit.model.Product
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddProductFrag : Fragment() {
    private val viewModel: AddProductViewModel by viewModels()
    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    val stockUnitList = listOf("Numbers (Nos)", "Kilogram (Kg)",
                               "Liter (L)", "Milliliter (ml)", "Bag (Bag)",
                                "Bundle (Bdl)", "Cans (Can)", "Case (Case)",
                                "Cartons (ctn)", "Dozen (Dzn)", "Meter (Mtr)",
                                "Packs (Pac)", "Piece (Pcs)", "Pair (Prs)",
                                "Quintal (Qtl)", "Roll (Rol)", "Square Feet (Sqf)",
                                "Square Meter (Sqm)", "Tablets (Tbs)","Jar (Jar)")

    val taxTypeList = listOf("Price includes Tax",
        "Price is without Tax",
        "Zero Rated Tax",
        "Exempt Tax")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCategories()
        viewModel.getProducts()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        onClickListeners()
        setupSpinner()
        setupSpinnerStockUnit(stockUnitList)
        setupSpinnerTaxType(taxTypeList)
        observers()
        return binding.root
    }

    private fun observers(){
        viewModel.products.observe(viewLifecycleOwner){
            Log.e("observe",it.data.toString())
        }

        viewModel.productsStatus.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun onClickListeners(){
        binding.btnAdd.setOnClickListener{
            viewModel.addproductObj(requireContext(),binding.etProductName.text.toString(),getProduct())
        }
    }

    private fun getProduct():Product{
        return Product(userId = Config.userId,
            productName = binding.etProductName.text.toString(),
            category = binding.dropdown.text.toString(),
            productPrice = binding.etProductPrice.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productCost = binding.etProductCost.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productMrp = binding.etProductMrp.text.toString().takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0,
            productBarcode = binding.etBarcode.text.toString(),
            productStockUnit = binding.dropdownStockUnit.text.toString(),
            productTax = 0.0,
            productStock = binding.etCurrentStock.text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 0,
            productDefaultQty = binding.etDefaultQty.text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 0,
            isSynced = 0)
    }


    private fun setupSpinner() {
      val adapter = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            liveDataItems = viewModel.getCategories()
        )
        binding.dropdown.setAdapter(adapter)
        binding.dropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            Toast.makeText(requireContext(), selectedItem, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinnerStockUnit(itemList: List<String>) {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownStockUnit.setAdapter(adapterStockUnit)
        binding.dropdownStockUnit.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            Toast.makeText(requireContext(), selectedItem, Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupSpinnerTaxType(itemList: List<String>) {
        val adapterStockUnit = GenericSpinnerAdapter(
            context = requireContext(),
            layoutResId = R.layout.dropdown_item, // Use your custom layout
            bindVariableId = BR.item,
            staticItems = itemList
        )
        binding.dropdownTaxType.setAdapter(adapterStockUnit)
        binding.dropdownTaxType.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapterStockUnit.getItem(position)
            Toast.makeText(requireContext(), selectedItem, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}