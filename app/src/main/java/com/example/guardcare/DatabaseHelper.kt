package com.example.guardcare

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "GuardCareDatabase"
        private const val DATABASE_VERSION = 1

        // User Table
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"

        // Child Table
        private const val TABLE_CHILDREN = "children"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_SEX = "sex"
        private const val COLUMN_HEIGHT = "height"
        private const val COLUMN_WEIGHT = "weight"

        // Nutritional Status Table
        private const val TABLE_NUTRITIONAL_STATUS = "nutritional_status"
        private const val COLUMN_CHILD_ID = "child_id"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_DATE = "date"

        // Growth Data Table
        private const val TABLE_GROWTH_DATA = "growth_data"
        private const val COLUMN_METRIC = "metric"
        private const val COLUMN_AGE_GROUP = "age_group"
        private const val COLUMN_Z_SCORES = "z_scores"
        private const val COLUMN_MSV = "msv"
        private const val COLUMN_SDV = "sdv"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Users Table
        val createUsersTable = """CREATE TABLE $TABLE_USERS (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_EMAIL TEXT UNIQUE,
            $COLUMN_PHONE TEXT UNIQUE,
            $COLUMN_PASSWORD TEXT
        )"""
        db.execSQL(createUsersTable)

        // Create Children Table
        val createChildrenTable = """CREATE TABLE $TABLE_CHILDREN (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT,
            $COLUMN_AGE INTEGER,
            $COLUMN_SEX TEXT,
            $COLUMN_HEIGHT REAL,
            $COLUMN_WEIGHT REAL
        )"""
        db.execSQL(createChildrenTable)

        // Create Nutritional Status Table
        val createNutritionalStatusTable = """CREATE TABLE $TABLE_NUTRITIONAL_STATUS (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_CHILD_ID INTEGER,
            $COLUMN_STATUS TEXT,
            $COLUMN_DATE TEXT,
            FOREIGN KEY($COLUMN_CHILD_ID) REFERENCES $TABLE_CHILDREN($COLUMN_ID)
        )"""
        db.execSQL(createNutritionalStatusTable)

        // Create Growth Data Table
        val createGrowthDataTable = """CREATE TABLE $TABLE_GROWTH_DATA (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_METRIC TEXT,
            $COLUMN_AGE_GROUP TEXT,
            $COLUMN_SEX TEXT,
            $COLUMN_Z_SCORES TEXT,
            $COLUMN_MSV REAL,
            $COLUMN_SDV REAL
        )"""
        db.execSQL(createGrowthDataTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHILDREN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NUTRITIONAL_STATUS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GROWTH_DATA")
        onCreate(db)
    }

    // Check if any user exists in the database
    fun checkIfUserExists(): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            null,
            null,
            null,
            null,
            null,
            "1"
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Insert user during signup
    fun insertUser(email: String, phone: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_PHONE, phone)
            put(COLUMN_PASSWORD, password)
        }

        return try {
            db.insertOrThrow(TABLE_USERS, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error inserting user", e)
            -1L
        }
    }

    // Insert child data
    fun insertChildData(name: String, age: Int, sex: String, height: Double, weight: Double): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_AGE, age)
            put(COLUMN_SEX, sex)
            put(COLUMN_HEIGHT, height)
            put(COLUMN_WEIGHT, weight)
        }

        return try {
            db.insertOrThrow(TABLE_CHILDREN, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error inserting child data", e)
            -1L
        }
    }

    // Insert nutritional status
    fun insertNutritionalStatus(childId: Int, status: String, date: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHILD_ID, childId)
            put(COLUMN_STATUS, status)
            put(COLUMN_DATE, date)
        }

        return try {
            db.insertOrThrow(TABLE_NUTRITIONAL_STATUS, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error inserting nutritional status", e)
            -1L
        }
    }

    // Check email login
    fun checkEmailLogin(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val projection = arrayOf(COLUMN_ID)
        val selection = "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(email, password)

        val cursor = db.query(
            TABLE_USERS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Check phone login
    fun checkPhoneLogin(phone: String, password: String): Boolean {
        val db = this.readableDatabase
        val projection = arrayOf(COLUMN_ID)
        val selection = "$COLUMN_PHONE = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(phone, password)

        val cursor = db.query(
            TABLE_USERS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Insert growth data
    fun insertGrowthData(growthData: GrowthData): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_METRIC, growthData.metric)
            put(COLUMN_AGE_GROUP, growthData.ageGroup)
            put(COLUMN_SEX, growthData.sex)
            put(COLUMN_Z_SCORES, growthData.zScores.toString())
            put(COLUMN_MSV, growthData.msv)
            put(COLUMN_SDV, growthData.sdv)
        }

        return db.insert(TABLE_GROWTH_DATA, null, values)
    }
}