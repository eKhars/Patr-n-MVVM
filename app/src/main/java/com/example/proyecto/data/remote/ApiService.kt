package com.example.proyecto.data.remote

import com.example.proyecto.data.model.AuthResponse
import com.example.proyecto.data.model.ProductData
import com.example.proyecto.data.model.RegisterUser
import com.example.proyecto.data.model.User
import com.example.proyecto.data.model.UserData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body credentials: User): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body userData: RegisterUser): Response<AuthResponse>

    @GET("auth/verify")
    suspend fun verifyToken(@Header("Authorization") token: String): Response<UserData>

    @GET("client/client/{id}")
    suspend fun getClientProfile(
        @Path("id") id: String,
        @Header("Cookie") token: String
    ): Response<UserData>

    @PUT("client/client/{id}")
    suspend fun updateClientProfile(
        @Path("id") id: String,
        @Header("Cookie") token: String,
        @Body profileData: Map<String, String>
    ): Response<UserData>

    @GET("products")
    suspend fun getProducts(
        @Header("Cookie") token: String
    ): Response<List<ProductData>>

    @Multipart
    @POST("products")
    suspend fun createProduct(
        @Header("Cookie") token: String,
        @Part("name") name: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ProductData>

    @Multipart
    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Header("Cookie") token: String,
        @Part("name") name: RequestBody,
        @Part("quantity") quantity: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ProductData>

    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: String,
        @Header("Cookie") token: String
    ): Response<Unit>
}