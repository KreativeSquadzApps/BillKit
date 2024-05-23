package com.kreativesquadz.billkit.api

import androidx.lifecycle.LiveData
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.model.CompanyDetails
import com.kreativesquadz.billkit.model.Customer
import com.kreativesquadz.billkit.model.Invoice
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("/api/invoices")
    fun loadInvoices(): LiveData<ApiResponse<List<Invoice>>>

    @Headers(
        "X-API-KEY: " + Config.API_Key, "Content-Type: application/json"
    )
    @POST("/api/addinvoice")
    suspend fun createInvoice(@Body invoice: Invoice): Response<ApiStatus>

    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("api/companyDetails")
    fun loadCompanyDetails(): LiveData<ApiResponse<List<CompanyDetails>>>

    @Headers("X-API-KEY: " + Config.API_Key)
    @PUT( "/api/updatecompanydetails")
    suspend fun updateCompanyDetails(@Body companyDetails: CompanyDetails?): Response<ApiStatus>

    @Headers("X-API-KEY: " + Config.API_Key)
    @GET("/api/customers")
    fun loadAllCustomers(): LiveData<ApiResponse<List<Customer>>>

    @Headers("X-API-KEY: " + Config.API_Key)
    @POST("/api/addcustomers")
    suspend fun addCustomer(@Body customer: Customer?): Response<ApiStatus>
}
