package io.github.kmpstore.presentation.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.Category
import kmpstore.shared.ui.generated.resources.Res
import kmpstore.shared.ui.generated.resources.arrow_drop_down
import kmpstore.shared.ui.generated.resources.arrow_drop_up
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun CategoryTreeItem(
    category: Category,
    categoriesByParent: Map<String?, List<Category>>,
    openCategories: Set<Category>,
    onIntent: (DrawerIntent) -> Unit,
    onCategoryClick: (String, String) -> Unit,
    depth: Int = 0
) {
    val subCategories = remember(category.id, categoriesByParent) {
        categoriesByParent[category.id] ?: emptyList()
    }
    val hasChildren = subCategories.isNotEmpty()

    // Check state from ViewModel instead of local mutableStateOf
    val isExpanded = openCategories.contains(category)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
//                    if (hasChildren) {
//                        val intent = if (isExpanded) DrawerIntent.CloseCategory(category) else DrawerIntent.OpenCategory(category)
//                        onIntent(intent)
//                    } else {
                    onCategoryClick(category.id, category.name)
                    onIntent(DrawerIntent.CloseDrawer) // Close drawer automatically on final selection
//                    }
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(start = (depth * 16).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.name,
                style = if (depth == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (hasChildren) {
                Icon(
                    painter = painterResource(if (isExpanded) Res.drawable.arrow_drop_up else Res.drawable.arrow_drop_down),
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.clickable {
                        val intent = if (isExpanded) DrawerIntent.CloseCategory(category) else DrawerIntent.OpenCategory(category)
                        onIntent(intent)
                    }
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column {
                subCategories.forEach { childCategory ->
                    CategoryTreeItem(
                        category = childCategory,
                        categoriesByParent = categoriesByParent,
                        openCategories = openCategories,
                        onIntent = onIntent,
                        onCategoryClick = onCategoryClick,
                        depth = depth + 1
                    )
                }
            }
        }
    }
}