package com.example.guardcare

import android.content.Context
import com.opencsv.CSVReader
import java.io.InputStreamReader
import java.io.IOException

data class GrowthData(
    val metric: String, // E.g., "Ihfa", "wfa"
    val ageGroup: String, // E.g., "0-to-2-years", "2-to-5-years"
    val sex: String, // E.g., "boys", "girls"
    val zScores: Map<String, Double>, // Z-scores as age to Z-score map
    val msv: Double, // Median Standard Value
    val sdv: Double  // Standard Deviation Value
)

class DataManager {

    // Function to load all CSV files and parse their content
    fun loadAllData(context: Context, dbHelper: DatabaseHelper): List<GrowthData> {
        val files = listOf(
            "lhfa_boys_0-to-2-years_zscores.csv", "lhfa_boys_0-to-13-weeks_zscores.csv",
            "lhfa_boys_2-to-5-years_zscores.csv", "lhfa_girls_0-to-2-years_zscores.csv",
            "lhfa_girls_0-to-13-weeks_zscores.csv", "lhfa_girls_2-to-5-years_zscores.csv",
            "wfa_boys_0-to-5-years_zscores.csv", "wfa_boys_0-to-13-weeks_zscores.csv",
            "wfa_girls_0-to-5-years_zscores.csv", "wfa_girls_0-to-13-weeks_zscores.csv",
            "wfh_boys_2-to-5-years_zscores.csv", "wfh_girls_2-to-5-years_zscores.csv",
            "wfl_boys_0-to-2-years_zscores.csv", "wfl_girls_0-to-2-years_zscores.csv"
        )

        val allGrowthData = mutableListOf<GrowthData>()

        // Iterate over each CSV file, parse it, and add the result to allGrowthData
        for (file in files) {
            try {
                val growthData = parseCSV(context, file) // Parse the CSV file
                allGrowthData.add(growthData) // Add parsed data to the list

                // Insert parsed growth data into the database
                dbHelper.insertGrowthData(growthData)
            } catch (e: Exception) {
                // Log the error or handle it appropriately
                println("Error parsing file $file: ${e.message}")
            }
        }

        return allGrowthData
    }

    // Function to parse each Z-score CSV file with improved error handling
    fun parseCSV(context: Context, fileName: String): GrowthData {
        try {
            val reader = CSVReader(InputStreamReader(context.assets.open(fileName)))
            val rows = mutableListOf<Array<String>>()

            reader.readNext()  // Skip the header row
            var row: Array<String>?
            while (reader.readNext().also { row = it } != null) {
                rows.add(row!!)
            }
            reader.close()

            // Extract the metric (like "Ihfa", "wfa"), age group (like "0-to-2-years"), and sex (like "boys", "girls") from the file name
            val metric = extractMetricFromFileName(fileName)
            val ageGroup = extractAgeGroupFromFileName(fileName)
            val sex = extractSexFromFileName(fileName)

            // Improved parsing to handle different number formats
            val zScores = mutableMapOf<String, Double>()
            for (r in rows) {
                val age = r[0] // Assume the first column is the age group or age

                // Handle potential semicolon-separated values or other number formats
                val zScoreValue = r[1].split(";").firstOrNull()?.trim() ?: r[1]
                val zScore = try {
                    zScoreValue.replace(",", ".").toDouble()
                } catch (e: NumberFormatException) {
                    // If parsing fails, use a default value or skip
                    0.0
                }
                zScores[age] = zScore
            }

            // More robust MSV and SDV extraction with error handling
            val msv = try {
                rows.firstOrNull()?.getOrNull(2)?.replace(",", ".")?.toDouble() ?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }

            val sdv = try {
                rows.firstOrNull()?.getOrNull(3)?.replace(",", ".")?.toDouble() ?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }

            // Return the organized GrowthData
            return GrowthData(
                metric = metric,
                ageGroup = ageGroup,
                sex = sex,
                zScores = zScores,
                msv = msv,
                sdv = sdv
            )
        } catch (e: IOException) {
            // Handle file reading errors
            throw RuntimeException("Error reading CSV file: ${e.message}", e)
        }
    }

    // Helper functions to extract metric, age group, and sex from the file name (unchanged)
    private fun extractMetricFromFileName(fileName: String): String {
        return fileName.split("_")[0] // E.g., "Ihfa", "wfa"
    }

    private fun extractAgeGroupFromFileName(fileName: String): String {
        return fileName.split("_")[1] + "-" + fileName.split("_")[2] // E.g., "0-to-2-years", "0-to-5-years"
    }

    private fun extractSexFromFileName(fileName: String): String {
        return if (fileName.contains("boys")) "boys" else "girls" // Extracting sex based on the file name
    }
}