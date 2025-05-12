package com.example.guardcare

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbHelper = DatabaseHelper(this)  // Initialize DatabaseHelper
        val dataManager = DataManager()

        // Load all data from CSV files and insert the data into the database
        val allGrowthData = dataManager.loadAllData(this, dbHelper)

        // Find UI components
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val heightEditText = findViewById<EditText>(R.id.heightEditText)
        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        val sexSpinner = findViewById<Spinner>(R.id.sexSpinner)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)

        // Set up click listener for the calculate button
        calculateButton.setOnClickListener {
            // Get user input
            val name = nameEditText.text.toString()
            val age = ageEditText.text.toString().toInt()
            val sex = sexSpinner.selectedItem.toString()
            val height = heightEditText.text.toString().toDouble()
            val weight = weightEditText.text.toString().toDouble()

            // Insert the child’s data into the database
            val childId = dbHelper.insertChildData(name, age, sex, height, weight)

            // Calculate the nutritional status based on the parsed growth data
            val nutritionalStatus = calculateZScore(age, sex, height, weight, allGrowthData)

            // Insert the nutritional status into the database
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dbHelper.insertNutritionalStatus(childId.toInt(), nutritionalStatus, currentDate)

            // Display the result
            statusTextView.text = "Nutritional Status: $nutritionalStatus"
        }
    }

    // Function to calculate Z-score and determine nutritional status
    fun calculateZScore(age: Int, sex: String, height: Double, weight: Double, growthData: List<GrowthData>): String {
        // Find the appropriate data for each metric using the correct metric
        val heightGrowthData = growthData.find {
            it.metric == "lhfa" &&
                    it.ageGroup == "${age}-to-${age + 2}-years" &&
                    it.sex == sex
        }
        val weightGrowthData = growthData.find {
            it.metric == "wfa" &&
                    it.ageGroup == "${age}-to-${age + 2}-years" &&
                    it.sex == sex
        }
        val weightForLengthGrowthData = growthData.find {
            it.metric == "wfl" &&
                    it.ageGroup == "${age}-to-${age + 2}-years" &&
                    it.sex == sex
        }

        // Calculate Z-scores for height, weight, and weight for length/height
        val heightZScore = heightGrowthData?.let {
            (height - it.msv) / it.sdv
        } ?: 0.0

        val weightZScore = weightGrowthData?.let {
            (weight - it.msv) / it.sdv
        } ?: 0.0

        val weightForLengthZScore = weightForLengthGrowthData?.let {
            (weight - it.msv) / it.sdv
        } ?: 0.0

        // Determine nutritional status based on the Z-scores
        return when {
            weightZScore < -2 -> "Underweight"
            weightZScore > 2 -> "Obese"
            weightZScore in -2.0..1.0 -> "Normal"
            heightZScore < -2 -> "Stunted"
            weightForLengthZScore < -2 -> "WFL Underweight"
            else -> "Normal"
        }
    }

}
