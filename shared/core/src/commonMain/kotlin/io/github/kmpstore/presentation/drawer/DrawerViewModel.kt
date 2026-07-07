package io.github.kmpstore.presentation.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.github.kmpstore.CategoryQueries
import io.github.kmpstore.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DrawerViewModel(
    val categoryQueries: CategoryQueries,
): ViewModel() {

    private val _state = MutableStateFlow(DrawerState())
    val state: StateFlow<DrawerState> = _state

    init {
        loadCategories()
    }

    fun onIntent(intent: DrawerIntent) {
        when (intent) {
            is DrawerIntent.OpenDrawer -> openDrawer()
            is DrawerIntent.CloseDrawer -> closeDrawer()
            is DrawerIntent.OpenCategory -> openCategory(intent.category)
            is DrawerIntent.CloseCategory -> closeCategory(intent.category)
        }
    }

    private fun openDrawer() {
        _state.update { state ->
            state.copy(isOpen = true)
        }
    }

    private fun closeDrawer() {
        _state.update { state ->
            state.copy(isOpen = false)
        }
    }

    private fun loadCategories() = viewModelScope.launch {
        categoryQueries.selectAllCategories()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .collect { categories ->
                _state.update { state ->
                    state.copy(categoriesByParent = categories.groupBy { it.parent_id })
                }
            }
    }

    private fun openCategory(category: Category) {
        _state.update { state ->
            val openCategories = state.openCategories
            state.copy(openCategories = openCategories + category)
        }
    }

    private fun closeCategory(category: Category) {
        _state.update { state ->
            val openCategories = state.openCategories
            state.copy(openCategories = openCategories - category)
        }
    }
}