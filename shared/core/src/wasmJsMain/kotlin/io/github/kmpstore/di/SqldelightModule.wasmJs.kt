package io.github.kmpstore.di

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.koin.dsl.module
import org.w3c.dom.Worker

actual val platformSqldelightModule = module {
    single<DriverFactory> { DriverFactory(get()) }
}

actual class DriverFactory(private val schema: SqlSchema<QueryResult.AsyncValue<Unit>>) {
    actual suspend fun createDriver(): SqlDriver = WebWorkerDriver(
        jsWorker()
    ).also { schema.create(it).await() }
}

@OptIn(ExperimentalWasmJsInterop::class)
internal fun jsWorker(): Worker =
    js("""new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")
    //js("""new Worker(new URL("sqldelight-sqlite-wasm-worker/sqlitewasm.worker.js", import.meta.url))""")