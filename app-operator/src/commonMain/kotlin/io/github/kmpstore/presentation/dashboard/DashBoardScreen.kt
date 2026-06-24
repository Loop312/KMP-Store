package io.github.kmpstore.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmpstore.domain.model.DeliveryStatus
import io.github.kmpstore.domain.model.Order
import io.github.kmpstore.presentation.operator.OperatorIntent
import io.github.kmpstore.presentation.operator.OperatorTab
import io.github.kmpstore.presentation.operator.OperatorViewModel
import io.github.kmpstore.theme.ThemeToggleButton
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorDashboardScreen(viewModel: OperatorViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { errorMessage ->
            println(errorMessage)
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.handleIntent(OperatorIntent.DismissError)
        }
    }

    // Local state to manage the selected order popup
    var activeDialogOrder by remember { mutableStateOf<Order?>(null) }
    // If an order is selected, show the popup window overlay
    activeDialogOrder?.let { order ->
        OrderDetailsDialog(
            order = order,
            detailedItems = state.selectedOrderDetails,
            isLoading = state.isDetailsLoading,
            onDismiss = {
                activeDialogOrder = null
                viewModel.handleIntent(OperatorIntent.ClearOrderDetails)
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Merchant Operator Hub") }, actions = { ThemeToggleButton() }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = state.currentTab == OperatorTab.AVAILABLE,
                    onClick = { viewModel.handleIntent(OperatorIntent.ChangeTab(OperatorTab.AVAILABLE)) },
                    label = { Text("Available") },
                    icon = { /* Icon */ }
                )
                NavigationBarItem(
                    selected = state.currentTab == OperatorTab.ACTIVE || state.currentTab == OperatorTab.MANIFEST,
                    onClick = { viewModel.handleIntent(OperatorIntent.ChangeTab(OperatorTab.ACTIVE)) },
                    label = { Text("Active") },
                    icon = { /* Icon */ }
                )
                NavigationBarItem(
                    selected = state.currentTab == OperatorTab.HISTORY,
                    onClick = { viewModel.handleIntent(OperatorIntent.ChangeTab(OperatorTab.HISTORY)) },
                    label = { Text("History") },
                    icon = { /* Icon */ }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (state.currentTab) {
                OperatorTab.AVAILABLE -> OrderList(
                    orders = state.availableOrders,
                    onOrderClick = { order ->
                        activeDialogOrder = order
                        viewModel.handleIntent(OperatorIntent.ViewOrderDetails(order))
                    },
                    onAction = { order -> viewModel.handleIntent(OperatorIntent.ClaimOrder(order.id)) }
                )
                OperatorTab.ACTIVE -> Column {
                    Button(
                        onClick = {
                            viewModel.handleIntent(OperatorIntent.LoadManifest)
                            viewModel.handleIntent(OperatorIntent.ChangeTab(OperatorTab.MANIFEST))
                        },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("View Cargo Manifest Aggregation")
                    }
                    OrderList(
                        orders = state.activeDeliveries,
                        onOrderClick = { order ->
                            activeDialogOrder = order
                            viewModel.handleIntent(OperatorIntent.ViewOrderDetails(order))
                        }, // Track click
                        onAction = { order ->
                            if (order.status == DeliveryStatus.SHIPPED) {
                                viewModel.handleIntent(OperatorIntent.UpdateStatus(order.id, DeliveryStatus.DELIVERED))
                            }
                            if (order.status == DeliveryStatus.PROCESSING) {
                                viewModel.handleIntent(OperatorIntent.DropOrder(order.id))
                            }
                        })
                }
                OperatorTab.MANIFEST -> ManifestScreen(
                    state = state,
                    onStartDelivery = {
                        viewModel.handleIntent(OperatorIntent.StartDelivery)
                        viewModel.handleIntent(OperatorIntent.ChangeTab(OperatorTab.ACTIVE))
                    },
                    onToggleItemCollected = { priceId ->
                        viewModel.handleIntent(OperatorIntent.ToggleManifestItemCollected(priceId))
                    },
                    onBack = { viewModel.handleIntent(OperatorIntent.ChangeTab(OperatorTab.ACTIVE)) }
                )
                OperatorTab.HISTORY -> OrderList(
                    orders = state.historyOrders,
                    onOrderClick = { order ->
                        activeDialogOrder = order
                        viewModel.handleIntent(OperatorIntent.ViewOrderDetails(order))
                    },
                    onAction = null
                )
            }
        }
    }
}