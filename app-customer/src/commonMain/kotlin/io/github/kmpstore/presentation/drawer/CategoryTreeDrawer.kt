package io.github.kmpstore.presentation.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryTreeDrawer(
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
            }
            // causes drawer to remain open when screen is changed and drawer is also on that screen
//            else if (drawerValue == DrawerValue.Open) {
//                viewModel.onIntent(DrawerIntent.OpenDrawer)
//            }
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
                        CategoryTreeItem(
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