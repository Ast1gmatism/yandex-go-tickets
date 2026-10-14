package com.trolleygo.tickets

import android.content.Context
import com.trolleygo.tickets.data.local.SharedPrefsStorage
import com.trolleygo.tickets.data.local.Storage

/**
 * Точка сборки зависимостей (простой service locator).
 * Приложение хранит данные в SharedPreferences — как и было выбрано;
 * тесты подставляют реализацию в памяти.
 */
object AppGraph {

    @Volatile
    var storageFactory: (Context) -> Storage = { SharedPrefsStorage(it) }

    fun storage(context: Context): Storage = storageFactory(context.applicationContext)

    /** Возвращает хранение по умолчанию (нужно тестам после подмены). */
    fun reset() {
        storageFactory = { SharedPrefsStorage(it) }
    }
}
