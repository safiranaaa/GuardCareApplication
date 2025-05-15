package com.example.guardcare

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbHelper = DatabaseHelper(this)
        val dataManager = DataManager()

        // Load all data from CSV files and insert into database
        val allGrowthData = dataManager.loadAllData(this, dbHelper)

        // Find UI components
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val heightEditText = findViewById<EditText>(R.id.heightEditText)
        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        val sexSpinner = findViewById<Spinner>(R.id.sexSpinner)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)

        // Set up sex spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.sex_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sexSpinner.adapter = adapter
        }

        // Set up click listener for calculate button
        calculateButton.setOnClickListener {
            try {
                // Get user input
                val name = nameEditText.text.toString()
                val age = ageEditText.text.toString().toIntOrNull() ?: 0
                val sex = sexSpinner.selectedItem.toString().lowercase()
                val height = heightEditText.text.toString().toDoubleOrNull() ?: 0.0
                val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0

                // Validate input
                if (name.isEmpty() || age == 0 || height == 0.0 || weight == 0.0) {
                    Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Insert child data
                val childId = dbHelper.insertChildData(name, age, sex, height, weight)

                if (childId == -1L) {
                    Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Calculate nutritional status
                val nutritionalStatus = calculateZScore(age, sex, height, weight, allGrowthData)

                // Insert nutritional status
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                dbHelper.insertNutritionalStatus(childId.toInt(), nutritionalStatus, currentDate)

                // Display result
                statusTextView.text = "Nutritional Status: $nutritionalStatus"
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateZScore(age: Int, sex: String, height: Double, weight: Double, growthData: List<GrowthData>): String {
        // Find appropriate data for each metric
        val heightData = growthData.find {
            it.metric == "lhfa" && it.sex == sex && age in 0..5
        }
        val weightData = growthData.find {
            it.metric == "wfa" && it.sex == sex && age in 0..5
        }
        val weightForHeightData = if (age <= 2) {
            growthData.find {
                it.metric == "wfl" && it.sex == sex
            }
        } else {
            growthData.find {
                it.metric == "wfh" && it.sex == sex
            }
        }

        // Calculate Z-scores
        val heightZScore = heightData?.let {
            (height - it.msv) / it.sdv
        } ?: 0.0

        val weightZScore = weightData?.let {
            (weight - it.msv) / it.sdv
        } ?: 0.0

        val weightForHeightZScore = weightForHeightData?.let {
            (weight - it.msv) / it.sdv
        } ?: 0.0

        // Determine nutritional status
        return when {
            heightZScore < -3.0 -> "Severely Stunted"
            heightZScore < -2.0 -> "Stunted"
            weightZScore < -3.0 -> "Severely Underweight"
            weightZScore < -2.0 -> "Underweight"
            weightForHeightZScore < -3.0 -> "Severely Wasted"
            weightForHeightZScore < -2.0 -> "Wasted"
            weightZScore > 2.0 -> "Overweight"
            weightZScore > 3.0 -> "Obese"
            else -> "Normal"
        }
    }
}