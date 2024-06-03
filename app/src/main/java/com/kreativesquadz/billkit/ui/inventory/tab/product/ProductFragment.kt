package com.kreativesquadz.billkit.ui.inventory.tab.product

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.GenericAdapter
import com.kreativesquadz.billkit.databinding.FragmentProductBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Product
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductFragment : Fragment() {
    private val viewModel: ProductViewModel by viewModels()
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GenericAdapter<Product>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getProducts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        onClickListeners()
        setupRecyclerView()
        observers()
        return binding.root
    }

    private fun observers(){
        viewModel.products.observe(viewLifecycleOwner) {
            it.data?.let {
                adapter.submitList(it)
            }
        }
    }

    private fun onClickListeners() {
        binding.btnAddProduct.setOnClickListener {
            findNavController().navigate(R.id.action_inventoryFrag_to_addProductFrag)
        }

    }

    private fun setupRecyclerView() {
        adapter = GenericAdapter(
            viewModel.products.value?.data ?: emptyList(),
            object : OnItemClickListener<Product> {
                override fun onItemClick(item: Product) {
                    // Handle item click
                }
            },
            R.layout.item_product,
            BR.product // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

}