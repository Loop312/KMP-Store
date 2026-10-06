# Architecture

## Modules

| Module | Role | Depends on |
|---|---|---|
| `shared/core` | Non-UI: domain models/repos, Supabase client, SQLDelight cache, MVI ViewModels, Koin, Ktor, Coil `ImageLoader` | — |
| `shared/ui` | Shared Compose UI: `theme/`, `presentation/auth/*`, `ProductCard`, `ProductImage`, `ScrollBarStyle` | `shared/core` |
| `shared/operator` | Operator-only: `domain/model/{Order,DeliveryStatus,ManifestItem,AddressDetails}`, `OperatorRepository`, `SupabaseOperatorRepository`, `Operator{ViewModel,State,Intent}`, `OperatorModule` | `shared/core` |
| `app-customer` | Storefront: Catalog/Category/Product/Cart/Login + Navigation3 | `core + ui` |
| `app-operator` | Dashboard: `OperatorDashboardScreen` + login gate, no Navigation3 | `core + ui + operator` |

All KMP targets: `androidTarget + jvm (desktop) + js/browser + wasmJs/browser + iosArm64 + iosSimulatorArm64`. `app-*` add `compose.desktop.application { mainClass = io.github.kmpstore.MainKt }`; `app-customer` web adds `navigation3-browser` for `BindBrowserNavigation`.

## MVI

Convention per feature: `*State` (data class) + `*Intent` (sealed) + `*ViewModel : ViewModel` (`MutableStateFlow` + `onIntent()`) + `*UiEffect` (`Channel`, where needed).

- `presentation/catalog/Catalog{ViewModel,State,Intent}` — `combine(categories, recursiveProductsMap)` + `refreshProducts()` on launch, `searchProducts()` via SQLDelight `LIKE`.
- `presentation/catalog/CategoryViewModel`, `presentation/product/Product{ViewModel,State,Intent,UiEffect}`, `presentation/cart/Cart{ViewModel,State,Intent,UiEffect}`, `presentation/auth/Login{ViewModel,State,Intent}` + `AuthMode` (EmailPassword/OAuth/MagicLink/OTP), `presentation/drawer/Drawer{...}`.
- Operator: `presentation/operator/Operator{ViewModel,State,Intent}` — `currentTab (AVAILABLE/ACTIVE/MANIFEST/HISTORY)` → `flatMapLatest` on `observe*Orders`; intents `Claim/Drop/UpdateStatus/ViewDetails/LoadManifest/ToggleCollected/StartDelivery`.

## DI (Koin 4.2.1 BOM)

- `shared/core/.../di/AuthModule.kt`: `single<SupabaseClient> { initSupabaseClient() }`, `single<AuthRepository>`, `factory<LoginViewModel>`.
- `shared/core/.../di/CatalogModule.kt`: `single<ProductRepository> { SupabaseProductRepository(...) }`, `single<CartRepository> { RealCartRepository }`, `single<ImageLoader>`, `factory<Catalog/Category/Drawer/Product/CartViewModel>`.
- `shared/core/.../di/NetworkModule.kt` + `expect platformNetworkModule` — actuals: Android/JVM `OkHttp`, iOS `Darwin`, JS/WasmJs `Js` (Ktor clients).
- `shared/core/.../di/SqldelightModule.kt` + `expect DriverFactory.createDriver(): SqlDriver` — actuals: Android `AndroidSqliteDriver`, JVM `JdbcSqliteDriver`, iOS `NativeSqliteDriver`, JS/WasmJs `WebWorkerDriver(sqljs.worker.js)`. Exposes `Database.Schema`, `product/category/cart_item/category_productsQueries`.
- `shared/operator/.../di/OperatorModule.kt`: `single<OperatorRepository> { SupabaseOperatorRepository(get, get) }`, `factory<OperatorViewModel>`.
- Boot: `app-customer/.../App.kt` loads `authModule + catalogModule + sqldelightModule`, async-creates `Database(driver)`, gates `Nav()` on `isDbReady` + optional `VERIFICATION_MESSAGE` dialog; `app-operator/.../App.kt` adds `operatorModule`, gates on `auth.sessionStatus` → `OperatorDashboardScreen` else `LoginScreen`.

Supabase client (`data/remote/ClientFactory.kt`): `createSupabaseClient(BuildKonfig.SUPABASE_URL/KEY) { install(Auth, Postgrest, Functions, Realtime); KotlinXSerializer(ignoreUnknownKeys) }`.

## SQLDelight cache (`shared/core/.../sqldelight/io/github/kmpstore/`)

- `product.sq`: `product(id, name, description, image_url, price, currency, price_id UNIQUE, stock)` + `insertProduct` (upsert), `selectAllProducts`, `selectProductById`, `selectProductsByCategoryId`, `selectProductsByCategoryIdRecursive` (CTE tree), `selectAllCategoryProductsRecursive`, `searchProducts`, `selectProductsByPriceIds`.
- `category.sq`: `category(id, name, slug, parent_id)` + `insertCategory`, `selectAllCategories`.
- `category_products.sq`: junction `PK(product_id, category_id)` + `selectAll/deleteAll/deleteByCategoryId/insertCategoryProduct`.
- `cart_item.sq`: `cart_item(product_id PK, quantity, added_at)` + upsert/decrement/remove/clear, `selectCartDetails (JOIN product + subtotal)`, `selectCartTotal`, `getCartSize`.

Pattern: repos expose `queries.asFlow().mapToList/mapToOneOrNull(Dispatchers.Default)`; `refresh*()` fetches Supabase RPC then atomic `transaction {}`. See `data/repository/SupabaseProductRepository.kt` (`getStoreFront`, `refreshProducts` via `get_store_sync_payload → StoreSyncDto`) and `RealCartRepository.kt`.

## Navigation3 (customer only)

- `app-customer/.../navigation/Route.kt`: `@Serializable sealed Route : NavKey { Login, ProductCatalog, CategoryDetail(id, name), ProductDetail(id), Cart }`.
- `app-customer/.../navigation/Nav.kt`: `mutableStateListOf(ProductCatalog)`, `NavDisplay(backStack, entryProvider { ... })`, `expect BindBrowserNavigation(backStack)` with per-platform actuals (`webMain` real browser binding via `navigation3-browser:0.3.1`, others no-op).
- Operator has no `navigation/` — `App.kt` swaps screens directly; dashboard in `app-operator/.../presentation/dashboard/{DashBoardScreen,ManifestScreen,OrderCard,OrderList,OrderDetailsDialog,StatusBadge}.kt`.

## Operator realtime + manifest

`SupabaseOperatorRepository` (`shared/operator/.../data/repository/SupabaseOperatorRepository.kt`):

- `observeAvailableOrders()` — `selectAsFlow` filtered `status IN (pending, processing)` → client keeps `PENDING` (unclaimed pool).
- `observeActiveDeliveries()` — all statuses → client keeps `PROCESSING/SHIPPED` (my work).
- `observeOrderHistory()` — `DELIVERED/CANCELLED`.
- `claimOrder()` sets `operator_id = me, status = PROCESSING` (guard `status = pending`); `dropOrder()` clears `operator_id`, resets `PENDING` (guard: only assignee, `status = processing`); `updateOrderStatus()` writes `status` (table grant restricts operator writes to `operator_id, status` only).
- `getAggregateManifest()` — fetches my `PROCESSING` orders, sums `items` maps (`{price_id: qty}` from Stripe session `metadata`), resolves `priceIds` via `productRepository.getProductsByPriceIds()` (SQLDelight), returns `List<ManifestItem(priceId, quantity, product)>` for the packing list. Storing `price_id:quantity` in metadata avoids Stripe line-item bloat.
