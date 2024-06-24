package com.kreativesquadz.billkit.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kreativesquadz.billkit.databinding.ItemCategoryHomeBinding
import com.kreativesquadz.billkit.interfaces.OnItemCatListener
import com.kreativesquadz.billkit.interfaces.OnItemClickListener
import com.kreativesquadz.billkit.model.Category

class GenericAdapterCategory<T>(
    private var items: List<Category>,
    private val listener: OnItemCatListener<T>,
    private val layoutResId: Int,
    private val bindVariableId: Int
) : RecyclerView.Adapter<GenericAdapterCategory.ViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemCategoryHomeBinding = DataBindingUtil.inflate(inflater, layoutResId, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item = items[position]
        holder.bind(item, listener, bindVariableId)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Category>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun isSelected(itemName: String) {
        Log.e("mmmm",itemName)
        items.forEach {
            Log.e("mmmmmmm",it.categoryName)
            if (it.categoryName.equals(itemName)){
                it.isSelected = 1
            } else {
                it.isSelected = 0
            }
        }
        notifyDataSetChanged()
    }

    class ViewHolder<T>(private val binding: ItemCategoryHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Category, listener: OnItemCatListener<T>, variableId: Int) {
            binding.setVariable(variableId, item)
            binding.root.setOnClickListener { listener.onItemCat(item) }
            if (item.isSelected == 0){
                binding.isSelected = false
            }else{
                binding.isSelected = true
            }
            binding.executePendingBindings()
        }
    }

}

