package com.kreativesquadz.billkit.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.withTransaction
import com.kreativesquadz.billkit.Dao.InvoiceDao
import com.kreativesquadz.billkit.Dao.SavedOrderDao
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.model.InvoiceItem
import com.kreativesquadz.billkit.model.SavedOrder
import com.kreativesquadz.billkit.model.SavedOrderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SavedOrderRepository @Inject constructor(val db: AppDatabase){
   private val savedOrderDao : SavedOrderDao = db.savedOrderDao()
   private val invoiceDao : InvoiceDao = db.invoiceDao()

   suspend fun saveOrder(savedOrder: SavedOrder, invoiceItems: List<InvoiceItem>) {
      withContext(Dispatchers.IO) {
         db.withTransaction {
            // Step 1: Insert SavedOrderEntity and get the generated orderId
            val savedOrderEntity = SavedOrderEntity(
               totalAmount = savedOrder.totalAmount,
               date = savedOrder.date,
               orderName = savedOrder.orderName
            )
            val orderId = savedOrderDao.insertSavedOrder(savedOrderEntity)
            Log.d("SavedOrderRepository", "OrderId: $orderId")

            invoiceItems.map{
               invoiceDao.insertInvoiceItem(it.copy(orderId = orderId))
               Log.e("SavedOrderRepositoryoooooo", it.copy(orderId = orderId).toString())
            }



            // Step 3: Save to remote server
//            try {
//               val response = apiService.saveOrder(savedOrder.copy(orderId = orderId))
//               if (!response.isSuccessful) {
//                  throw Exception("Failed to save order to server")
//               }
//            } catch (e: Exception) {
//               // Handle network failure
//               // You can add retry logic or mark the order as pending for future sync
//            }
         }
      }
   }

   suspend fun getSavedOrders(): List<SavedOrder> {
      return withContext(Dispatchers.IO) {
         val savedOrders = savedOrderDao.getSavedOrders()
         savedOrders.map { orderEntity ->
            val invoiceItems = invoiceDao.getInvoiceItemsByOrderId(orderEntity.orderId)
            SavedOrder(
               orderId = orderEntity.orderId,
               orderName = orderEntity.orderName,
               totalAmount = orderEntity.totalAmount,
               date = orderEntity.date,
               items = invoiceItems

            )
         }
      }
   }


   fun deleteSavedOrder(orderId: Long) {
      savedOrderDao.deleteSavedOrderById(orderId)
   }



}