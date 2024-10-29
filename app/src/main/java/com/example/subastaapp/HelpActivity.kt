package com.example.subastaapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HelpActivity : AppCompatActivity() {

    private lateinit var faqContainer: LinearLayout
    private lateinit var problemaInput: EditText
    private lateinit var enviarProblemaButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        faqContainer = findViewById(R.id.faq_container)
        problemaInput = findViewById(R.id.problema_input)
        enviarProblemaButton = findViewById(R.id.enviar_problema_button)

        loadFAQs()

        enviarProblemaButton.setOnClickListener {
            enviarProblema()
        }
    }

    private fun loadFAQs() {
        RetrofitInstance.api.getFAQs().enqueue(object : Callback<List<FAQ>> {
            override fun onResponse(call: Call<List<FAQ>>, response: Response<List<FAQ>>) {
                if (response.isSuccessful && response.body() != null) {
                    val faqs = response.body()
                    faqs?.forEach { faq ->
                        addFAQToView(faq.pregunta, faq.respuesta)
                    }
                } else {
                    Toast.makeText(this@HelpActivity, "No se pudieron cargar las preguntas frecuentes.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<FAQ>>, t: Throwable) {
                Toast.makeText(this@HelpActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addFAQToView(pregunta: String, respuesta: String) {
        val questionTextView = TextView(this).apply {
            text = "¿$pregunta?"
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(0, 16, 0, 4)
        }

        val answerTextView = TextView(this).apply {
            text = respuesta
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.darker_gray))
        }

        faqContainer.addView(questionTextView)
        faqContainer.addView(answerTextView)
    }

    private fun enviarProblema() {
        val descripcion = problemaInput.text.toString()
        if (descripcion.isNotEmpty()) {
            val problema = Problema(descripcion)
            RetrofitInstance.api.enviarProblema(problema).enqueue(object : Callback<Problema> {
                override fun onResponse(call: Call<Problema>, response: Response<Problema>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@HelpActivity, "Problema enviado", Toast.LENGTH_SHORT).show()
                        problemaInput.text.clear()
                    } else {
                        Toast.makeText(this@HelpActivity, "Error al enviar el problema", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Problema>, t: Throwable) {
                    Toast.makeText(this@HelpActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Por favor describe tu problema", Toast.LENGTH_SHORT).show()
        }
    }
}
