package io.github.kmpstore.di

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.github.kmpstore.DATABASE_NAME
import org.koin.dsl.module
import java.util.Properties

actual val platformSqldelightModule = module {
    single<DriverFactory> { DriverFactory(get()) }
}

actual class DriverFactory(private val schema: SqlSchema<QueryResult.AsyncValue<Unit>>) {
    actual suspend fun createDriver(): SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$DATABASE_NAME", Properties(), schema.synchronous())
        .also { schema.create(it).await() }
}