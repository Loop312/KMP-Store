# KMPStore

Full e-commerce suite written with Compose Multiplatform + MVI.

- **Catalog (customer) app** (`:app-customer`): storefront — browse categories/products, search, cart, Stripe checkout via Supabase Edge Function. SQLDelight offline cache, Navigation3.
- **Operator app** (`:app-operator`): fulfillment — claim orders, update `pending → processing → shipped → delivered/cancelled`, realtime updates, aggregate packing manifest. No Navigation3, single dashboard.
- **Backend:** Supabase (Postgres + Auth + Realtime + Edge Functions) + Stripe (via [Stripe Sync Engine](https://supabase.com/docs/guides/integrations/stripe) + custom `stripe-checkout` Edge Function).
- **DI:** Koin. **Secrets:** BuildKonfig + `local.properties` (keys never committed).

Targets: Android, iOS (`iosArm64`/`iosSimulatorArm64`), Desktop (JVM), Web (JS + WasmJS). See `settings.gradle.kts` for modules: `:app-customer`, `:app-operator`, `:shared:core`, `:shared:ui`, `:shared:operator`, `:server`.

## Docs

- [`docs/SETUP.md`](docs/SETUP.md) — full setup: Stripe → Supabase → SQL order → operator role/RLS/Realtime → Edge Function → `local.properties` → run apps → `collectBuilds`.
- [`docs/CUSTOMIZATION.md`](docs/CUSTOMIZATION.md) — landing page, theme, store name, cart limits, categories via Stripe metadata, checkout countries/URLs.
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — MVI, Koin modules, SQLDelight cache, Navigation3, operator realtime + manifest (`price_id:quantity` metadata trick).

## Quickstart

```bash
# 1. Supabase + Stripe first (creates stripe.products/prices tables)
#    Follow docs/SETUP.md §1-4, run SQL in /supabase/sql in order,
#    deploy /supabase/edge-functions/stripe-checkout.ts

# 2. Client config (see shared/core/build.gradle.kts BuildKonfig block)
cp local.properties.example local.properties  # then uncomment + fill in keys
# REQUIRED: SUPABASE_URL, SUPABASE_KEY
# RECOMMENDED: STORE_NAME
# OPTIONAL: VERIFICATION_MESSAGE (dialog on launch), MAX_CART_SIZE (keep ≤ 30, Stripe metadata limit)

# 3. Run
./gradlew :app-customer:wasmJsBrowserDevelopmentRun  # customer web (wasm, fast)
./gradlew :app-customer:jsBrowserDevelopmentRun      # customer web (js, compat)
./gradlew :app-customer:run                         # customer desktop
./gradlew :app-operator:run                         # operator desktop
./gradlew :app-customer:assembleDebug               # customer android
./gradlew :app-operator:assembleDebug                # operator android
# iOS: open iosApp/ in Xcode

# 4. Collect outputs for all systems except iOS into !buildOutputs/<module>/{android,js,wasm,desktop}/
./gradlew collectBuilds   # root build.gradle.kts (desktop = host OS; iOS via Xcode)
```

> `collectBuilds` runs `assemble` + `createDistributable` on all subprojects, then copies APKs (`**/*.apk`), `build/dist/js|wasmJs/productionExecutable/`, `build/compose/binaries/main/app/` into `!buildOutputs/`. Covers all systems **except iOS**. Desktop binary matches the OS you build on. `:server` jar is **not** collected — grab it from its `build/` dir manually (It's only if you want to build a custom backend but we're not using that in this project so you can ignore it).

## Checkout flow (unified for all platforms)

`CartViewModel` (`shared/core/.../presentation/cart/CartViewModel.kt:104-157`) → `supabase.functions.invoke("stripe-checkout", {items:[{price: priceId, quantity}]})` → Edge Function creates `stripe.checkout.sessions.create({line_items: items, metadata: {priceId: quantity}, mode:'payment', shipping_address_collection, success_url/cancel_url})` → returns `{url}` → app opens URL, empties cart. Operator manifest later re-expands `metadata` (`price_id:quantity`) to avoid Stripe line-item bloat — see `SupabaseOperatorRepository.getAggregateManifest()`.

## Customization TL;DR

```kotlin
// shared/ui/.../theme/Color.kt
private val PrimaryColor = Color(0xFF6200EE)   // change hexes to retheme
var isDarkTheme by mutableStateOf(false)       // -> true to default to dark
```

```html
<!-- app-customer/src/webMain/resources/index.html : replace with your landing page -->
<a href="./app/" class="btn">Launch App</a> <!-- must keep ./app/ link -->
```

Full details: `docs/CUSTOMIZATION.md`.
