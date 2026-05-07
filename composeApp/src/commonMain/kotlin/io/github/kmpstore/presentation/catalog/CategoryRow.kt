package io.github.kmpstore.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.github.kmpstore.domain.model.Category
import io.github.kmpstore.pad

@Composable
fun CategoryRow(
    category: Category,
    onCategoryClick: (String, String) -> Unit,
    onProductClick: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(pad)) {
        // Category Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(pad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { onCategoryClick(category.id, category.name) }) {
                Text("See All", style = MaterialTheme.typography.labelLarge)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = pad),
            horizontalArrangement = Arrangement.spacedBy(pad) // Increased spacing for a cleaner look
        ) {
            items(
                items = category.products,
                key = { it.id } // Performance boost for Lazy lists
            ) { product ->
                ProductCard(product, onProductClick)
            }
        }
    }
}