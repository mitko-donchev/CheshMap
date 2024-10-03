package com.epicmillennium.cheshmap.data.source.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListConverter {
    @TypeConverter
    fun listToString(list: List<String>): String = Gson().toJson(list)

    @TypeConverter
    fun stringToList(string: String): List<String> = try {
        Gson().fromJson<List<String>>(string) //using extension function
    } catch (e: Exception) {
        emptyList()
    }
}

inline fun <reified T> Gson.fromJson(json: String): List<String> =
    fromJson(json, object : TypeToken<T>() {}.type)