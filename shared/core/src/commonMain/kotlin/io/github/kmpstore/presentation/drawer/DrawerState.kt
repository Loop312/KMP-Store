package io.github.kmpstore.presentation.drawer

import io.github.kmpstore.Category

data class DrawerState(
    val categoriesByParent: Map<String?, List<Category>> = emptyMap(),
    val isOpen: Boolean = false,
    val openCategories: Set<Category> = emptySet(),
)
