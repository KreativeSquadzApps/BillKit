package com.kreativesquadz.billkit.adapter

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.kreativesquadz.billkit.utils.CategorySelection

fun <T> showPopupMenuCategory(
    context: Context,
    anchorView: View,
    items: List<T>, // Generic list of items
    itemToString: (T) -> String, // Convert item to string for display
    onItemSelected: (CategorySelection<T>) -> Unit // Callback for item selection
) {
    val popupMenu = PopupMenu(context, anchorView)

    // Add "All" as the first item
    popupMenu.menu.add(0, 0, 0, "All")

    // Add categories dynamically
    items.forEachIndexed { index, item ->
        popupMenu.menu.add(0, index + 1, index + 1, itemToString(item)) // index+1 for "All"
    }

    // Handle category selection
    popupMenu.setOnMenuItemClickListener { menuItem ->
        if (menuItem.itemId == 0) {
            // "All" selected
            onItemSelected(CategorySelection.All)
        } else {
            // Specific category selected
            val selectedItem = items[menuItem.itemId - 1] // Adjust index due to "All" at 0
            onItemSelected(CategorySelection.SelectedCategory(selectedItem))
        }
        true
    }

    popupMenu.show()
}
