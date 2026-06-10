package io.github.kmpstore.presentation.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.STORE_NAME
import io.github.kmpstore.domain.model.Product
import io.github.kmpstore.pad
import io.github.kmpstore.theme.ThemeToggleButton
import kmpstore.shared.ui.generated.resources.Res
import kmpstore.shared.ui.generated.resources.account_circle
import kmpstore.shared.ui.generated.resources.ic_search
import kmpstore.shared.ui.generated.resources.shopping_cart
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogTopBar(
    searchQuery: String,
    searchResults: List<Product>,
    onCartClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onIntent: (CatalogIntent) -> Unit
) {
    val isExpanded = searchQuery.isNotBlank()

    TopAppBar(
        title = {
            // Use Alignment.CenterVertically to force the Title Text and SearchBar to line up perfectly
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isExpanded) {
                    Text(
                        text = "$STORE_NAME's Catalog",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(end = pad) // Gives the SearchBar breathing room
                    )
                }
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            leadingIcon = { Icon(painterResource(Res.drawable.ic_search), null) },
                            query = searchQuery,
                            onQueryChange = { onIntent(CatalogIntent.OnQueryChange(it)) },
                            onSearch = { searchResults.firstOrNull()?.id?.let { p1 -> onProductClick(p1) } },
                            expanded = isExpanded,
                            onExpandedChange = {},
                            placeholder = { Text("Search products...") }
                        )
                    },
                    expanded = isExpanded,
                    onExpandedChange = {},
                    modifier = Modifier
                        .then(if (isExpanded) Modifier.fillMaxWidth() else Modifier.width(400.dp))
                        .padding(vertical = 4.dp),
                    content = {
                        SearchResultsList(
                            searchResults = searchResults,
                            searchQuery = searchQuery,
                            onProductClick = onProductClick
                        )
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.padding(horizontal = pad),
        actions = {
            // Center the action icons perfectly relative to the TopAppBar content height
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(64.dp) // Standard TopAppBar height line-up
            ) {
                if (!isExpanded) {
                    ThemeToggleButton(Modifier.size(32.dp))
                    Spacer(Modifier.width(pad / 2))
                    Icon(
                        painter = painterResource(Res.drawable.shopping_cart),
                        contentDescription = "shopping_cart",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onCartClick)
                    )
                    Spacer(Modifier.width(pad / 2))
                    Icon(
                        painter = painterResource(Res.drawable.account_circle),
                        contentDescription = "account",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    )
}