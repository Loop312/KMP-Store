package io.github.kmpstore.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.kmpstore.domain.model.Category
import io.github.kmpstore.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabaseProductRepository(
    private val supabase: SupabaseClient,
    // private val productDao: ProductDao // plug Room in here soon
) : ProductRepository {

    override fun getStoreFront(): Flow<List<Category>> = flow {
        // Single request to fetch categories AND their nested products
        val response = supabase.postgrest["categories"]
            .select(Columns.raw("*, products(*)"))
            .decodeList<Category>()
        println(response)
        emit(response)
    }

    override suspend fun refreshProducts(): Result<Unit> = runCatching {
        // Logic for Room sync goes here
    }
}