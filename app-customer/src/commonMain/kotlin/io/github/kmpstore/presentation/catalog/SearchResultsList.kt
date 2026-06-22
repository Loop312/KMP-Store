package io.github.kmpstore.presentation.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.pad
import io.github.kmpstore.presentation.ProductImage

@Composable
fun SearchResultsList(
    searchResults: List<Product>,
    searchQuery: String,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
        Text(
            text = "No products found for \"$searchQuery\"",
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.padding(pad)
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(pad),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = searchResults, key = { it.id }) { product ->
            ListItem(
                leadingContent = { ProductImage(product, Modifier.size(64.dp)) },
                headlineContent = { Text(product.name) },
                supportingContent = { Text(product.description) },
                trailingContent = { Text(product.formattedPrice) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProductClick(product.id) }
            )
        }
    }
}