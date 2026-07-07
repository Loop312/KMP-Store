package io.github.kmpstore.presentation.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.Category
import kmpstore.shared.ui.generated.resources.Res
import kmpstore.shared.ui.generated.resources.arrow_drop_up
import kmpstore.shared.ui.generated.resources.arrow_drop_down
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DrawerCategoryTree(
    viewModel: DrawerViewModel = koinViewModel(),
    onCategoryClick: (String, String) -> Unit,
    content: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Setup the material drawer state and watch it
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed,
        confirmStateChange = { drawerValue ->
            // Intercept manual swipes/scrim clicks to notify the ViewModel
            if (drawerValue == DrawerValue.Closed) {
                viewModel.onIntent(DrawerIntent.CloseDrawer)
            } else {
                viewModel.onIntent(DrawerIntent.OpenDrawer)
            }
            true
        }
    )

    // One-way sync from ViewModel state to Drawer Animation state
    LaunchedEffect(state.isOpen) {
        if (state.isOpen && drawerState.isClosed) {
            drawerState.open()
        } else if (!state.isOpen && drawerState.isOpen) {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                val rootCategories = state.categoriesByParent[null] ?: emptyList()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(rootCategories, key = { it.id }) { rootCategory ->
                        DrawerCategoryTreeItem(
                            category = rootCategory,
                            categoriesByParent = state.categoriesByParent,
                            openCategories = state.openCategories, // Pass open tracking down
                            onIntent = viewModel::onIntent,       // Pass intent handler down
                            onCategoryClick = onCategoryClick
                        )
                    }
                }
            }
        },
        content = content
    )
}

@Composable
fun DrawerCategoryTreeItem(
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
                    DrawerCategoryTreeItem(
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