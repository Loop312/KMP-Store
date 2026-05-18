package io.github.kmpstore.presentation.product

import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.domain.model.Resource

data class ProductState(val product: Resource<Product> = Resource.Loading, val cartQuantity: Int = 0)
