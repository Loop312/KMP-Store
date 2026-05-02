package io.github.kmpstore.di

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import app.cash.sqldelight.driver.worker.expected.Worker
import org.koin.dsl.module

actual val platformSqldelightModule = module {
    single<DriverFactory> { DriverFactory(get()) }
}

actual class DriverFactory(private val schema: SqlSchema<QueryResult.AsyncValue<Unit>>) {
    actual suspend fun createDriver(): SqlDriver = WebWorkerDriver(
        Worker(
            js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
        )
    ).also { schema.create(it).await() }
}