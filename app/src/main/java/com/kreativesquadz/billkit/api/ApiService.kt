package com.kreativesquadz.billkit.api

import androidx.lifecycle.LiveData
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.CreditNote
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.GST
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.LoginResponse
import com.kreativesquadz.billkit.model.Product
import com.kreativesquadz.billkit.model.SavedOrder
import com.kreativesquadz.billkit.model.SavedOrderEntity
import com.kreativesquadz.billkit.model.Staff
import com.kreativesquadz.billkit.model.User
import com.kreativesquadz.billkit.model.UserSetting
import com.kreativesquadz.billkit.model.request.CreditNoteStatusUpdate
import com.kreativesquadz.billkit.model.request.InvoiceRequest
import com.kreativesquadz.billkit.model.request.InvoiceStatusUpdate
import com.kreativesquadz.billkit.model.request.LoginRequest
import com.kreativesquadz.billkit.model.request.ProductStockRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @Headers("X-API-KEY: " + Config.API_Key)
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse


    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("api/invoices")
    fun loadInvoices(): LiveData<ApiResponse<List<Invoice>>>

    @PUT( "/api/updateinvoicestatus")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateInvoiceStatus(@Body invoiceStatusUpdate: InvoiceStatusUpdate?): Response<ApiStatus>



    @POST("/api/addinvoice")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun createInvoice(@Body invoiceRequest: InvoiceRequest): Response<ApiStatus>


    @GET("api/companyDetails/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadCompanyDetails(@Path("userId") userId: Long): LiveData<ApiResponse<CompanyDetails>>


    @PUT( "/api/updatecompanydetails")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateCompanyDetails(@Body companyDetails: CompanyDetails?): Response<ApiStatus>


    @GET("/api/customers")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadAllCustomers(): LiveData<ApiResponse<List<Customer>>>


    @POST("/api/addcustomers")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addCustomer(@Body customer: Customer): Response<ApiStatus>


    @POST("/api/user_settings")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addUserSetting(@Body userSetting: UserSetting): Response<ApiStatus>


    @GET("/api/user_settings/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadUserSetting(@Path("userId") userId: Long): LiveData<ApiResponse<UserSetting>>


    @PUT( "/api/updateuser_settings")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateUserSetting(@Body userSetting: UserSetting?): Response<ApiStatus>



    @POST("/api/addcategories")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addCategories(@Body category: Category): Response<ApiStatus>


    @GET("/api/categories/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadCategories(@Path("userId") userId: Long): LiveData<ApiResponse<List<Category>>>

    @DELETE("/api/deletecategory/{id}")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun deleteCategory(@Path("id") id: Long): Response<ResponseBody>


    @GET("/api/products/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadProducts(@Path("userId") userId: Long): LiveData<ApiResponse<List<Product>>>


    @POST("/api/addproducts")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addProduct(@Body product: Product): Response<ApiStatus>

    @DELETE("/api/deleteproduct/{productId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun deleteProduct(@Path("productId") id: Long): Response<ResponseBody>

    @PUT( "/api/updateproductstock")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateProductStock(@Body productStockRequest: ProductStockRequest?): Response<ApiStatus>

    @PUT("/api/updateproduct")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateProduct(@Body product: Product?): Response<ApiStatus>



    @GET("/api/credit_notes/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadCreditNote(@Path("userId") userId: Long): LiveData<ApiResponse<List<CreditNote>>>

    @POST("/api/addcredit_notes")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addCreditNote(@Body creditNote: CreditNote): Response<ApiStatus>

    @PUT( "/api/updatecredit_notes")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateCreditNoteStatus(@Body creditNoteStatusUpdate: CreditNoteStatusUpdate?): Response<ApiStatus>


    @GET("/api/staff/{adminId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadStaff(@Path("adminId") adminId: Long): LiveData<ApiResponse<List<Staff>>>


    @POST("/api/addstaff")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addStaff(@Body staff: Staff): Response<ApiStatus>


    @GET("/api/gsttax/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadAllGstTax(@Path("userId") userId: Int): LiveData<ApiResponse<List<GST>>>


    @POST("/api/addgsttax")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addGstTax(@Body gst: GST): Response<ApiStatus>


    @POST("orders")
    fun saveOrder(@Body savedOrder: SavedOrder): LiveData<ApiResponse<Boolean>>

    @GET("orders")
    fun getSavedOrders(): LiveData<ApiResponse<List<SavedOrderEntity>>>

}
