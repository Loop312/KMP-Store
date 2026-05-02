package io.github.kmpstore.di

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import io.github.kmpstore.shared.Database
import org.koin.core.module.Module
import org.koin.dsl.module

val sqldelightModule = module {
    includes(platformSqldelightModule)
    //single<Database> { Database(driver = get()) }
    single<SqlSchema<QueryResult.AsyncValue<Unit>>> { Database.Schema }
    single { get<Database>().productQueries }
    single { get<Database>().categoryQueries }
}

expect val platformSqldelightModule: Module

expect class DriverFactory {
    suspend fun createDriver(): SqlDriver
}