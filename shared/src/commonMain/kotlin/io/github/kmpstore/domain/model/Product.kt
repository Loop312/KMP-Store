package io.github.kmpstore.domain.model

import io.github.kmpstore.SelectAllCategoryProductsRecursive

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Long,
    val imageUrl: String,
    val currency: String = "CAD",
    val priceId: String,
    val stock: Long?
) {
    constructor(product: io.github.kmpstore.Product) : this (
        id = product.id,
        name = product.name,
        description = product.description ?: "",
        price = product.price,
        imageUrl = product.image_url ?: "https://placehold.co/400",
        currency = product.currency,
        priceId = product.price_id,
        stock = product.stock,
    )

    constructor(product: SelectAllCategoryProductsRecursive) : this (
        id = product.id,
        name = product.name,
        description = product.description ?: "",
        price = product.price,
        imageUrl = product.image_url ?: "https://placehold.co/400",
        currency = product.currency,
        priceId = product.price_id,
        stock = product.stock,
    )

    val formattedPrice: String
        get() {
            val dollars = price / 100
            val cents = (price % 100).toString().padStart(2, '0')
            return "\$$dollars.$cents $currency"
        }
}