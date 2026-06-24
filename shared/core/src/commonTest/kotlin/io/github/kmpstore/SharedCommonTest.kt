package io.github.kmpstore

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.kmpstore.di.authModule
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun connectToDatabase() = runTest {
        val koin = startKoin {
            modules(authModule)
        }.koin
        val client: SupabaseClient = koin.get<SupabaseClient>()
        println(client)

        println("getting test_table from database")
        val table = client.from("test_table").select(Columns.ALL).data
        println("TABLE:\n$table")
        println("__________________")
    }
}