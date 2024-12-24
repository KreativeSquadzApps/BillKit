package com.kreativesquadz.hisabkitab.api

import androidx.lifecycle.LiveData
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.model.Category
import com.kreativesquadz.hisabkitab.model.CompanyDetails
import com.kreativesquadz.hisabkitab.model.CreditNote
import com.kreativesquadz.hisabkitab.model.Customer
import com.kreativesquadz.hisabkitab.model.CustomerCreditDetail
import com.kreativesquadz.hisabkitab.model.settings.GST
import com.kreativesquadz.hisabkitab.model.Invoice
import com.kreativesquadz.hisabkitab.model.InvoicePrefixNumber
import com.kreativesquadz.hisabkitab.model.LoginResponse
import com.kreativesquadz.hisabkitab.model.Product
import com.kreativesquadz.hisabkitab.model.SavedOrder
import com.kreativesquadz.hisabkitab.model.SavedOrderEntity
import com.kreativesquadz.hisabkitab.model.Staff
import com.kreativesquadz.hisabkitab.model.settings.UserSetting
import com.kreativesquadz.hisabkitab.model.request.CreditNoteStatusUpdate
import com.kreativesquadz.hisabkitab.model.request.CustomerCreditRequest
import com.kreativesquadz.hisabkitab.model.request.InvoiceRequest
import com.kreativesquadz.hisabkitab.model.request.InvoiceStatusUpdate
import com.kreativesquadz.hisabkitab.model.request.LoginRequest
import com.kreativesquadz.hisabkitab.model.request.ProductStockRequest
import com.kreativesquadz.hisabkitab.model.request.ProductStockRequestByName
import com.kreativesquadz.hisabkitab.model.settings.Packaging
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @Headers("X-API-KEY: " + Config.API_Key)
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse?


    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("api/invoices")
    fun loadInvoices(): LiveData<ApiResponse<List<Invoice>>>

    @PUT( "/api/updateinvoicestatus")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateInvoiceStatus(@Body invoiceStatusUpdate: InvoiceStatusUpdate?): Response<ApiStatus>


    @POST("/api/addinvoice")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun createInvoice(@Body invoiceRequest: InvoiceRequest): Response<ApiStatus>

    @PUT("/api/updateinvoice")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateInvoice(@Body invoiceRequest: InvoiceRequest): Response<ApiStatus>

    @POST("/api/addcompanyDetails")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addCompanyDetails(@Body companyDetails: CompanyDetails?): Response<ApiStatus>

    @Multipart
    @POST("/api/updateCompanyImage")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateCompanyImage(@Part("userId") userId: RequestBody, @Part image: MultipartBody.Part?): Response<ApiStatus>

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


    @POST("/api/addCustomerCreditDetail")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addCustomerCreditDetail(@Body customerCreditDetail: CustomerCreditDetail): Response<ApiStatus>



    @DELETE("/api/deletecustomer/{id}")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun deleteCustomer(@Path("id") id: Long): Response<ResponseBody>

    @PUT( "/api/updatecustomer")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateCustomer(@Body customer: Customer?): Response<ApiStatus>


    @PUT( "/api/updateCustomerCredit")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateCustomerCredit(@Body customerCreditRequest: CustomerCreditRequest?): Response<ApiStatus>

    @POST("/api/user_settings")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addUserSetting(@Body userSetting: UserSetting): Response<ApiStatus>


    @POST("/api/addInvoicePrefixNumber")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addInvoicePrefixNumber(@Body invoicePrefixNumber: InvoicePrefixNumber): Response<ApiStatus>

    @GET("/api/invoicePrefixNumber/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadInvoicePrefixNumber(@Path("userId") userId: Long): LiveData<ApiResponse<List<InvoicePrefixNumber>>>

    @DELETE("/api/deleteInvoicePrefixNumber/{id}")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun deleteInvoicePrefixNumber(@Path("id") id: Long): Response<ResponseBody>

    @PUT( "/api/updateCompanyInvoicePrefixNumber")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateCompanyInvoicePrefixNumber(@Body invoicePrefixNumber: InvoicePrefixNumber?): Response<ApiStatus>



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

    @PUT( "/api/decrementproductstock")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun decrementProductStockByName(@Body productStockRequestByName: ProductStockRequestByName?): Response<ApiStatus>

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

    @DELETE("/api/deletestaff/{id}")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun deleteStaff(@Path("id") id: Long): Response<ResponseBody>

    @PUT( "/api/updatestaff")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun updateStaff(@Body staff: Staff?): Response<ApiStatus>


    @GET("/api/gsttax/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadAllGstTax(@Path("userId") userId: Int): LiveData<ApiResponse<List<GST>>>

    @GET("/api/loadpackaging/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    fun loadAllPackaging(@Path("userId") userId: Int): LiveData<ApiResponse<List<Packaging>>>


    @POST("/api/addgsttax")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addGstTax(@Body gst: GST): Response<ApiStatus>

    @POST("/api/addPackaging")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun addPackaging(@Body packaging: Packaging): Response<ApiStatus>

    @DELETE("/api/deleteGstTax/{id}")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun deleteGstTax(@Path("id") id: Int): Response<ResponseBody>


    @DELETE("/api/deletepackaging/{id}")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun deletePackaging(@Path("id") id: Int): Response<ResponseBody>



    @POST("orders")
    fun saveOrder(@Body savedOrder: SavedOrder): LiveData<ApiResponse<Boolean>>

    @GET("orders")
    fun getSavedOrders(): LiveData<ApiResponse<List<SavedOrderEntity>>>


    @DELETE("/api/reset/{userId}")
    @Headers("X-API-KEY: " + Config.API_Key)
    suspend fun hardReset(@Path("userId") userId: Long): Response<ResponseBody>


}