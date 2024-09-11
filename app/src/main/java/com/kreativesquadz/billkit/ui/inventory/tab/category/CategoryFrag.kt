package com.kreativesquadz.billkit.ui.inventory.tab.category

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.kreativesquadz.billkit.BR
import com.kreativesquadz.billkit.R
import com.kreativesquadz.billkit.adapter.CatogoryAdapter
import com.kreativesquadz.billkit.adapter.showCustomAlertDialog
import com.kreativesquadz.billkit.databinding.FragmentCategoryBinding
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.DialogData
import com.kreativesquadz.billkit.utils.collapse
import com.kreativesquadz.billkit.utils.expand
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CategoryFrag : Fragment() {
    private val viewModel: CategoryViewModel by viewModels()
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CatogoryAdapter<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getCategories()
        viewModel.getProducts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observers()
        onClickListeners()
        return binding.root
    }

    private fun onClickListeners(){
        binding.addCategory.setOnClickListener {
            if (binding.etCategory.text.toString().isEmpty()){
                binding.etCategory.error = "Please Enter Category Name"
                return@setOnClickListener
            }
            viewModel.addcategoryObj(requireContext(),binding.etCategory.text.toString())

        }
    }

    private fun observers(){
        viewModel.categoriesStatus.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(),it.message, Toast.LENGTH_SHORT).show()
            if (it.invoiceId==200){
                binding.etCategory.text.clear()
            }
        }

        viewModel.category.observe(viewLifecycleOwner) { categoryResponse ->
            categoryResponse.data?.let { categories ->
                // Set categories to the adapter
                adapter.submitList(categories)

                // Now observe products
                viewModel.products.observe(viewLifecycleOwner) { productResponse ->
                    productResponse.data?.let { products ->
                        // Filter products by category and set it to each Category in adapter
                        categories.forEach { category ->
                            val categoryProducts = products.filter { product -> product.category == category.categoryName }
                            // Update the adapter with the filtered products for the respective category
                            adapter.setProductsForCategory(category.categoryName, categoryProducts)
                        }
                    }
                }
            }
        }
    }

    private fun setupPopup(name : String ,action: () -> Unit){
        val dialogData = DialogData(
            title = "Delete Category",
            info = "Are you sure you want to Delete ${name} Category ?",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel"
        )

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


    private fun setupRecyclerView() {
        adapter = CatogoryAdapter(
            viewModel.category.value?.data ?: emptyList(),
            object : OnItemClickListener<Category> {
                override fun onItemClick(item: Category) {
                    val categoryPosition = viewModel.category.value?.data?.indexOf(item) ?: -1
                    val holder = binding.recyclerView.findViewHolderForAdapterPosition(categoryPosition)
                    if (holder is CatogoryAdapter.ViewHolder<*>) {
                        val binding = holder.binding as ViewDataBinding
                        val dropdownContent = binding.root.findViewById<View>(R.id.dropdown_content)
                        val header = binding.root.findViewById<TextView>(R.id.header)
                        if (dropdownContent.visibility == View.GONE) {
                            dropdownContent.expand() // Assuming you have an extension function for expanding the view
                            header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0)
                        } else {
                            dropdownContent.collapse() // Assuming you have an extension function for collapsing the view
                            header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
                        }

                }
             }
            },
            object : OnItemClickListener<Category> {
                override fun onItemClick(item: Category) {
                    setupPopup(item.categoryName){
                        viewModel.deleteCategory(requireContext(),item.categoryId)
                    }
                }
            },

            R.layout.item_category,
            BR.category // Variable ID generated by data binding
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
}