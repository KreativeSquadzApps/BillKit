package com.kreativesquadz.hisabkitab.model.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tax_settings")
data class TaxSettings(
    @PrimaryKey val id: Int = 1,  // Assuming one setting for simplicity
    val defaultTaxOption: TaxOption, // Enum or sealed class for tax options
    val selectedTaxPercentage: Float = 0.0f // Use if percentage tax is selected
)

sealed class TaxOption {
    data object PriceIncludesTax : TaxOption()
    data object PriceExcludesTax : TaxOption()
    data object ZeroRatedTax : TaxOption()
    data object ExemptTax : TaxOption()
}
