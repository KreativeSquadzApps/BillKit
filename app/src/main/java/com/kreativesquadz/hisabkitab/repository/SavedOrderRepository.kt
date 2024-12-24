package com.kreativesquadz.hisabkitab.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.kreativesquadz.hisabkitab.Dao.InvoiceDao
import com.kreativesquadz.hisabkitab.Dao.SavedOrderDao
import com.kreativesquadz.hisabkitab.Database.AppDatabase
import com.kreativesquadz.hisabkitab.api.ApiClient
import com.kreativesquadz.hisabkitab.api.ApiResponse
import com.kreativesquadz.hisabkitab.api.common.NetworkBoundResource
import com.kreativesquadz.hisabkitab.api.common.common.Resource
import com.kreativesquadz.hisabkitab.model.InvoiceItem
import com.kreativesquadz.hisabkitab.model.SavedOrder
import com.kreativesquadz.hisabkitab.model.SavedOrderEntity
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
   fun loadSavedOrders(userId: Long): LiveData<Resource<List<SavedOrderEntity>>> {
      return object : NetworkBoundResource<List<SavedOrderEntity>, List<SavedOrderEntity>>() {
         override fun saveCallResult(item: List<SavedOrderEntity>) {
            try {
               db.runInTransaction {
                  savedOrderDao.insertAllSavedOrders(item)
               }
            } catch (ex: Exception) {
               Log.e("TAG", ex.toString())
            }
         }

         override fun shouldFetch(data: List<SavedOrderEntity>?): Boolean {
            return true
         }

         override fun loadFromDb(): LiveData<List<SavedOrderEntity>> {
            return savedOrderDao.getSavedOrders()
         }

         override fun createCall(): LiveData<ApiResponse<List<SavedOrderEntity>>> {
            return ApiClient.getApiService().getSavedOrders()
         }
      }.asLiveData()
   }

   suspend fun getInvoiceItemsByOrderId(orderId: Long): List<InvoiceItem> {
      return invoiceDao.getInvoiceItemsByOrderId(orderId)
   }

   fun deleteSavedOrder(orderId: Long) {
      savedOrderDao.deleteSavedOrderById(orderId)
   }



}