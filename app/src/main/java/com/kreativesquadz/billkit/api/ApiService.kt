package com.kreativesquadz.billkit.api

import androidx.lifecycle.LiveData
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.Category
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import com.kreativesquadz.billkit.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("api/invoices")
    fun loadInvoices(): LiveData<ApiResponse<List<Invoice>>>

    @Headers(
        "X-API-KEY: " + Config.API_Key, "Content-Type: application/json"
    )
    @POST("/api/addinvoice")
    suspend fun createInvoice(@Body invoice: Invoice): Response<ApiStatus>

    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("api/companyDetails/{userId}")
    fun loadCompanyDetails(@Path("userId") userId: Long): LiveData<ApiResponse<CompanyDetails>>

    @Headers("X-API-KEY: " + Config.API_Key)
    @PUT( "/api/updatecompanydetails")
    suspend fun updateCompanyDetails(@Body companyDetails: CompanyDetails?): Response<ApiStatus>

    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("/api/customers")
    fun loadAllCustomers(): LiveData<ApiResponse<List<Customer>>>

    @Headers("X-API-KEY: " + Config.API_Key)
    @POST("/api/addcustomers")
    suspend fun addCustomer(@Body customer: Customer): Response<ApiStatus>

    @Headers("X-API-KEY: " + Config.API_Key)
    @POST("/api/addusers")
    suspend fun addUser(@Body user: User): Response<ApiStatus>

    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("/api/users/{userId}")
    fun loadUser(@Path("userId") userId: Long): LiveData<ApiResponse<List<User>>>


    @Headers("X-API-KEY: " + Config.API_Key)
    @POST("/api/addcategories")
    suspend fun addCategories(@Body category: Category): Response<ApiStatus>

    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("/api/categories/{userId}")
    fun loadCategories(@Path("userId") userId: Long): LiveData<ApiResponse<List<Category>>>


}
