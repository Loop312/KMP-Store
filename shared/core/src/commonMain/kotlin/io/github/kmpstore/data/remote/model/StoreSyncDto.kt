package io.github.kmpstore.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class StoreSyncDto (
    val categories: List<CategoryDto>,
    val products: List<ProductDto>,
    val junctions: List<CategoryProductsDto>
)