package com.kreativesquadz.billkit.ui.home.tab.sale

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentSaleBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.ui.home.tab.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SaleFragment : Fragment() {
    private val viewModel: SaleViewModel by viewModels()
    private var _binding: FragmentSaleBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<Product>
    private lateinit var adapterCat: GenericAdapter<Category>
    private val sharedViewModel : SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getProducts()
        viewModel.getCategories()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaleBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupRecyclerViewCat()
        observers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey("object") }?.apply {
        }
    }

    private fun observers(){
        viewModel.products.observe(viewLifecycleOwner) {
            it.data?.let {
                viewModel.filterProducts(it, viewModel.selectedCategory.value)
            }
        }
        viewModel.category.observe(viewLifecycleOwner) {
            it.data?.let {
                var filteredList = it
                filteredList = filteredList + Category(10000, Config.userId, "All", 0)
                filteredList = filteredList.sortedBy { it.categoryName }
                adapterCat.submitList(filteredList)
            }
        }
        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            viewModel.products.value?.data?.let { productList ->
                viewModel.filterProducts(productList, category)
            }
        }
        viewModel.filteredProducts.observe(viewLifecycleOwner) { filteredList ->
            adapter.submitList(filteredList)
        }

    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.products.value?.data ?: emptyList(),
            object : OnItemClickListener<Product> {
                override fun onItemClick(item: Product) {
                    sharedViewModel.addProduct(item)
                }
            },
            R.layout.item_product_home,
            BR.productHome // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
    }


    private fun setupRecyclerViewCat() {
        adapterCat = GenericAdapter(
            viewModel.category.value?.data ?: emptyList(),
            object : OnItemClickListener<Category> {
                override fun onItemClick(item: Category) {
                    viewModel.selectedCategory.value = item.categoryName
                }
            },
            R.layout.item_category_home,
            BR.category // Variable ID generated by data binding
        )
        binding.recyclerViewCat.adapter = adapterCat
        binding.recyclerViewCat.layoutManager = LinearLayoutManager(context)
    }
}