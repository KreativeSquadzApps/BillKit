package com.kreativesquadz.billkit.ui.inventory.tab.product

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.adapter.showPopupMenuCategory
import com.kreativesquadz.billkit.databinding.FragmentProductBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.ui.inventory.InventoryFragmentDirections
import com.kreativesquadz.billkit.utils.CategorySelection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductFragment : Fragment() {
    private val viewModel: ProductViewModel by viewModels()
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<Product>
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getProducts()
        viewModel.getCategories()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        onClickListeners()
        setupRecyclerView()
        observers()
        return binding.root
    }

    private fun observers(){
        binding.etProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.products.value?.data?.let {
                    if (selectedCategory != null){
                        adapter.submitList(it.filter { product ->
                            product.productName.contains(s.toString(), ignoreCase = true) &&
                                    product.category == selectedCategory
                        })
                        return
                    } else{
                        adapter.submitList(it.filter { product ->
                            product.productName.contains(s.toString(), ignoreCase = true)

                        })
                        return
                    }

                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        viewModel.products.observe(viewLifecycleOwner) {
            it.data?.let {
                adapter.submitList(it)
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) {
            it.data?.let {
            }
        }

    }

    private fun onClickListeners() {
        binding.btnAddProduct.setOnClickListener {
            findNavController().navigate(R.id.action_inventoryFrag_to_addProductFrag)
        }

            binding.filter.setOnClickListener { view ->
                showPopupMenuCategory(
                    context = requireContext(),
                    anchorView = view,
                    items = viewModel.categories.value?.data ?: emptyList(), // List of categories
                    itemToString = { it.categoryName },
                    onItemSelected = { selection ->
                        viewModel.products.value?.data?.let { products ->
                            when (selection) {
                                is CategorySelection.All -> {
                                    // Show all products if "All" is selected
                                    adapter.submitList(products)
                                    selectedCategory = null
                                }
                                is CategorySelection.SelectedCategory -> {
                                    selectedCategory = selection.category.categoryName
                                    // Filter products based on the selected category
                                    val filteredList = products.filter { product ->
                                        product.category == selection.category.categoryName
                                    }
                                    adapter.submitList(filteredList)
                                    selectedCategory = selection.category.categoryName
                                }

                            }
                        }
                    }
                )
            }



    }



    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.products.value?.data ?: emptyList(),
            object : OnItemClickListener<Product> {
                override fun onItemClick(item: Product) {

                    val action = InventoryFragmentDirections.actionInventoryFragToEditProductFragment(item)
                    findNavController().navigate(action)
                }
            },
            R.layout.item_product,
            BR.product // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

}