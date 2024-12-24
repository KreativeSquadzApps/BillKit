package com.kreativesquadz.hisabkitab.repository

import com.kreativesquadz.hisabkitab.Database.AppDatabase
import com.kreativesquadz.hisabkitab.model.CustomerCreditDetail
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.MergedCreditDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class CreditRepository @Inject constructor(val db: AppDatabase) {
    val creditDetailsDao = db.creditDetailsDao()
    val invoiceDao = db.invoiceDao()

    fun getMergedCreditDetails(customerId : Long): Flow<List<MergedCreditDetail>> {
        return combine(
            creditDetailsDao.getAllCustomerCreditDetails(customerId),
            invoiceDao.getAllInvoicesFlow(customerId)
        ) { customerCredits: List<CustomerCreditDetail>, invoices: List<Invoice> ->
            mergeCreditDetailsAndInvoices(customerCredits, invoices)
        }
    }

    private fun mergeCreditDetailsAndInvoices(
        customerCredits: List<CustomerCreditDetail>,
        invoices: List<Invoice>
    ): List<MergedCreditDetail> {
        val mergedDetails = mutableListOf<MergedCreditDetail>()

        customerCredits.forEach { credit ->
            mergedDetails.add(
                MergedCreditDetail(
                    date = credit.creditDate,
                    creditType = credit.creditType,
                    amount = credit.creditAmount
                )
            )
        }

        invoices.forEach { invoice ->
            mergedDetails.add(
                MergedCreditDetail(
                    date = invoice.invoiceDate,
                    creditType = "Inv: ${invoice.invoiceNumber}",
                    amount = invoice.creditAmount
                )
            )
        }

        return mergedDetails
            .filter { it.date.isNotEmpty() } // Filter based on the presence of the date
            .sortedByDescending { it.date }  // Sort by date in descending order (latest first)
    }


    fun getCustomerCreditDetails(customerId: Long): CustomerCreditDetail {
        return creditDetailsDao.getCustomerCreditDetail(customerId)
    }
}

