package com.kreativesquadz.billkit.utils

sealed class TaxType(val displayName: String) {
    object PriceIncludesTax : TaxType("Price includes Tax")
    object PriceWithoutTax : TaxType("Price is without Tax")
    object ZeroRatedTax : TaxType("Zero Rated Tax")
    object ExemptTax : TaxType("Exempt Tax")

    companion object {
        fun getList(): List<TaxType> = listOf(PriceIncludesTax, PriceWithoutTax, ZeroRatedTax, ExemptTax)
        fun fromString(value: String): TaxType? {
            return when (value) {
                PriceIncludesTax.displayName -> PriceIncludesTax
                PriceWithoutTax.displayName -> PriceWithoutTax
                ZeroRatedTax.displayName -> ZeroRatedTax
                ExemptTax.displayName -> ExemptTax
                else -> null // or handle it however you'd like if the string doesn't match
            }
        }
    }

}
