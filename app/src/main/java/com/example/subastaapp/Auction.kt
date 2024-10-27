package com.example.subastaapp

data class Auction(
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val startingPrice: Double = 0.0,
    val minimumIncrease: Double = 0.0,
    val endTime: Long = 0L // Tiempo de finalización en milisegundos
)
