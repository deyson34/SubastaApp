package com.example.subastaapp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CreateAuctionActivity : AppCompatActivity() {

    private lateinit var auctionImageView: ImageView
    private lateinit var categorySpinner: Spinner
    private var selectedImageUri: Uri? = null
    private var endTimeInMillis: Long = 0L // Variable para almacenar el tiempo de finalización
    private val PICK_IMAGE_REQUEST = 71

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_auction)

        val titleField = findViewById<EditText>(R.id.auctionTitle)
        val descriptionField = findViewById<EditText>(R.id.auctionDescription)
        val startingPriceField = findViewById<EditText>(R.id.startingPrice)
        val minimumIncreaseField = findViewById<EditText>(R.id.minimumIncrease)
        categorySpinner = findViewById(R.id.auctionCategorySpinner)
        auctionImageView = findViewById(R.id.auctionImageView)
        val createButton = findViewById<Button>(R.id.createAuctionButton)
        val uploadImageButton = findViewById<Button>(R.id.uploadImageButton)
        val selectEndDateButton = findViewById<Button>(R.id.selectEndDateButton)
        val endDateTextView = findViewById<TextView>(R.id.endDateTextView)

        loadCategories() // Método para cargar categorías desde Firebase

        // Botón para seleccionar la fecha y hora de finalización
        selectEndDateButton.setOnClickListener {
            selectEndDateTime(endDateTextView)
        }

        // Botón para seleccionar una imagen de la galería
        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Botón para crear la subasta con verificación adicional para el Spinner
        createButton.setOnClickListener {
            val title = titleField.text.toString()
            val description = descriptionField.text.toString()
            val startingPrice = startingPriceField.text.toString().toDoubleOrNull() ?: 0.0
            val minimumIncrease = minimumIncreaseField.text.toString().toDoubleOrNull() ?: 0.0

            // Verificación de valor del Spinner
            val category = if (categorySpinner.selectedItem != null) {
                categorySpinner.selectedItem.toString()
            } else {
                "No category selected"
            }

            if (title.isNotEmpty() && description.isNotEmpty() && selectedImageUri != null && endTimeInMillis > 0 && category != "No category selected") {
                uploadImageAndCreateAuction(title, description, startingPrice, minimumIncrease, category, endTimeInMillis)
            } else {
                Toast.makeText(this, "Completa todos los campos, selecciona una imagen y una fecha de finalización", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectEndDateTime(endDateTextView: TextView) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                endTimeInMillis = calendar.timeInMillis
                endDateTextView.text = "Fecha de finalización: ${calendar.time}"
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadCategories() {
        val categoriesRef = FirebaseDatabase.getInstance().getReference("categories")
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoriesList = mutableListOf<String>()

                if (snapshot.exists()) {
                    for (categorySnapshot in snapshot.children) {
                        // Verificar si el snapshot contiene el campo "name"
                        val name = categorySnapshot.child("name").getValue(String::class.java)
                        if (name != null) {
                            categoriesList.add(name)
                        } else {
                            println("No se encontró el campo 'name' en el nodo ${categorySnapshot.key}")
                        }
                    }
                } else {
                    println("El nodo 'categories' no existe en la base de datos")
                }

                // Si no se encontraron categorías, añade una opción predeterminada
                if (categoriesList.isEmpty()) {
                    categoriesList.add("No categories available")
                }

                // Configura el adaptador del Spinner
                val adapter = ArrayAdapter(this@CreateAuctionActivity, android.R.layout.simple_spinner_item, categoriesList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CreateAuctionActivity, "Error al cargar categorías: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadImageAndCreateAuction(
        title: String, description: String, startingPrice: Double,
        minimumIncrease: Double, category: String, endTime: Long
    ) {
        val storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("auction_images/${UUID.randomUUID()}.jpg")

        selectedImageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        createAuctionInDatabase(title, description, imageUrl, startingPrice, minimumIncrease, category, endTime)
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    Toast.makeText(this, "Error al subir la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun createAuctionInDatabase(
        title: String, description: String, imageUrl: String,
        startingPrice: Double, minimumIncrease: Double, category: String, endTime: Long
    ) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("auctions")
        val auctionId = databaseReference.push().key

        val auction = Auction(title, description, imageUrl, category, startingPrice, minimumIncrease, endTime)

        auctionId?.let {
            databaseReference.child(it).setValue(auction)
                .addOnSuccessListener {
                    Toast.makeText(this, "Subasta creada con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al crear la subasta", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            auctionImageView.setImageURI(selectedImageUri)
        }
    }
}
