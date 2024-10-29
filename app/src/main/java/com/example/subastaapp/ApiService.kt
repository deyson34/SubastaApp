package com.example.subastaapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class FAQ(val pregunta: String, val respuesta: String)
data class Problema(val descripcion: String)

interface ApiService {
    @GET("faqs/")
    fun getFAQs(): Call<List<FAQ>>

    @POST("problemas/")
    fun enviarProblema(@Body problema: Problema): Call<Problema>
}