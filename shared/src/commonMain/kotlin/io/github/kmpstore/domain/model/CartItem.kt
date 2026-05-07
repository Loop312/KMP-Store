package io.github.kmpstore.domain.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalPriceFormatted: String
        get() {
            // Calculate total in cents first to maintain precision
            val totalCents = product.price * quantity

            val dollars = totalCents / 100
            val cents = (totalCents % 100).toString().padStart(2, '0')

            return "Price: \$$dollars.$cents"
        }
}