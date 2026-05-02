package io.github.kmpstore.di

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.github.kmpstore.DATABASE_NAME
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformSqldelightModule = module {
    single<DriverFactory> { DriverFactory(get(), androidContext()) }
}

actual class DriverFactory(private val schema: SqlSchema<QueryResult.AsyncValue<Unit>>, private val context: Context) {
    actual suspend fun createDriver(): SqlDriver = AndroidSqliteDriver(schema.synchronous(), context, DATABASE_NAME)
}