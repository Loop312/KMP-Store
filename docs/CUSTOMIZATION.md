# Customization

## Landing page (web)

Replace `app-customer/src/webMain/resources/index.html` with your own page. Keep the link to the app:

```html
<a href="./app/" class="btn">Launch App</a>
```

The `app/` subpath is the Compose loader (`app-customer/src/webMain/resources/app/index.html` → `app-customer.js` → `ComposeViewport { App() }`). Don’t edit `app/index.html` for branding — only the top-level `index.html` (title, hero, styles).

## Theme

`shared/ui/src/commonMain/kotlin/io/github/kmpstore/theme/Color.kt`:

```kotlin
private val PrimaryColor = Color(0xFF6200EE)    // brand primary
private val SecondaryColor = Color(0xFF03DAC6)  // brand secondary
// LightColorScheme: background 0xFFFFFFFF, surface 0xFFF5F5F5, onBackground/onSurface 0xFF1D1B20
// DarkColorScheme:  background 0xFF000000, surface 0xFF1E1E1E, onBackground/onSurface 0xFFE6E1E5
var isDarkTheme by mutableStateOf(false)        // -> true to default to dark
```

Applied in `theme/AppTheme.kt` (`MaterialTheme(colorScheme = if (darkTheme) Dark else Light)`); toggle via `ThemeToggleButton.kt`. Just change hexes.

## Store name / cart size / launch dialog

`local.properties` (copy from `local.properties.example`, BuildKonfig, compile-time — rebuild after editing):

```properties
STORE_NAME=My Store            # -> Constants.STORE_NAME (titles, drawers)
MAX_CART_SIZE=25                                 # -> Constants.MAX_CART_SIZE (CartViewModel rejects more distinct items; keep ≤ 30, Stripe metadata limit)
VERIFICATION_MESSAGE=          # empty = no dialog; non-empty = blocking dialog in App.kt on launch
```

Defined in `shared/core/build.gradle.kts:117-150`, consumed in `Constants.kt:7-11`.

## Categories (no code — via Stripe)

Set on the Stripe product: `metadata.categories = "audio,headphones"`. The trigger in `supabase/sql/public/SyncStripeProductCategories.sql` auto-creates `public.categories` rows (`initcap(slug)` as name) + `category_products` links + zero-stock `operations.inventory` row on every `stripe.products` insert/update. Hierarchical sub-categories: set `parent_id` manually in `public.categories` (self-FK, `id <> parent_id`); the app’s recursive SQLDelight queries (`selectProductsByCategoryIdRecursive`) already walk the tree.

## Checkout countries / URLs

`supabase/edge-functions/stripe-checkout.ts:42-46`:

```ts
shipping_address_collection: { allowed_countries: ['CA'] }  // edit to your ship countries
success_url: `${projectUrl}/success`   // projectUrl = FRONTEND_URL secret
cancel_url: `${projectUrl}/cancel`
```

Redeploy after editing (`supabase functions deploy stripe-checkout`). Showing stock counts: uncomment the `JOIN operations.inventory` lines in `supabase/sql/public/ProductsView.sql`.
