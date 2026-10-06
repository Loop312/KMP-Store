# Setup — Supabase + Stripe + Apps

Order matters. Stripe first (tables must exist), then SQL, then roles/RLS, then Edge Function, then clients.

## 0. Prerequisites

- Supabase project (URL + `sb_publishable_*` anon key)
- Stripe account + secret key (`sk_*`)
- Supabase CLI + Stripe CLI (for local deploy/testing), Deno (Edge Functions run on Deno)
- Android SDK (`sdk.dir` in `local.properties`), Xcode for iOS, JDK 11+

## 1. Enable “Stripe Sync Engine” integration

In Supabase Dashboard → Integrations → **Stripe Sync Engine** → Connect with `STRIPE_API_KEY` (+ `STRIPE_WEBHOOK_SECRET` if prompted).

This creates and syncs the `stripe` schema the app depends on:

- `stripe.products(id, name, description, images, active, metadata, ...)`
- `stripe.prices(id, product, unit_amount, currency, active, ...)`

Verify in SQL editor: `select * from stripe.products limit 1;`

Manage the catalog **in Stripe Dashboard** (not Supabase):

- Product `active = true`, Price `active = true` or it’s hidden by `public.products` view.
- Categories come from product metadata: `metadata.categories = "slug1,slug2"` (comma-separated, lowercase slugs). Pretty name is auto-`initcap()`’d. Example: `metadata.categories = "audio,headphones"`.

## 2. Run SQL in `/supabase/sql` — in this order

Files are raw snippets (no `migrations/` folder, no `config.toml`). Run in Supabase SQL editor.

```
1. sql/operations/OrdersTable.sql        # schema operations, enum delivery_status, table operations.orders
2. sql/operations/InventoryTable.sql      # table operations.inventory (FK -> stripe.products.id)
3. sql/public/CategoryTables.sql          # public.categories (self-hierarchy) + public.category_products junction
4. sql/public/ProductsView.sql            # public.products view: stripe.products ⨝ stripe.prices (active only)
5. sql/public/StoreSyncPayload.sql        # RPC get_store_sync_payload() -> {categories, products, junctions} + get_category_sync_payload()
6. sql/public/SyncStripeProductCategories.sql  # trigger on_stripe_product_sync + backfill UPDATE
```

Notes:

- `InventoryTable.sql` bottom `INSERT ... SELECT FROM stripe.products` is commented out — uncomment once if Stripe was set up first and you want zero-stock rows for existing products. Otherwise the trigger in step 6 creates them going forward.
- `ProductsView.sql` has commented `JOIN operations.inventory` / `i.quantity AS stock`. Uncomment if you want `stock` exposed (used by `CartViewModel` stock guard).
- `SyncStripeProductCategories.sql` last line `UPDATE stripe.products SET name = default;` is a fake update to backfill categories/inventory for existing products. Safe to run once.
- `refreshCategory()` in `SupabaseProductRepository` calls RPC `get_category_sync_payload(requested_category_id)` (same file, `StoreSyncPayload.sql`) for single-category refresh; full `refreshProducts()` uses `get_store_sync_payload()`.

### Enable RLS (not in the SQL files — you must do it)

```sql
alter table public.categories enable row level security;
alter table public.category_products enable row level security;
alter table operations.orders enable row level security;
-- operations.inventory: leave RLS off (service_role/trigger only), or enable + add policy as needed
```

Then run policies:

```
7. sql/public/RLSPolicies.sql       # SELECT on categories + category_products TO public (open read)
8. sql/operations/RLSPolicies.sql   # SELECT/UPDATE on operations.orders TO operator (own-or-unassigned only)
```

Claim model: `operator_id = auth.uid() OR operator_id IS NULL`. No INSERT/DELETE for `operator`; orders are created by your Stripe webhook path (see §5).

## 3. Operator role

```sql
-- run once:
create role operator nologin;  -- skip if role already exists
-- sql/role/operator/OperationsPermissions.sql
-- sql/role/operator/PublicPermissions.sql
```

What those do (`OperationsPermissions.sql:1-12`, `PublicPermissions.sql:1-12`):

- `GRANT operator TO authenticator;` + `NOTIFY pgrst, 'reload config';` so PostgREST recognizes the role
- `GRANT SELECT, UPDATE (operator_id, status) ON operations.orders TO operator;` — column-level: can read everything visible via RLS but only touch `operator_id`/`status`
- `GRANT USAGE ON SCHEMA operations/public`, `EXECUTE ON FUNCTION get_store_sync_payload()`, `SELECT ON ALL TABLES IN SCHEMA public` + future tables
- `ALTER TABLE operations.orders REPLICA IDENTITY FULL;` — required for Realtime

Promote a user (`sql/role/operator/AddOperator.sql`):

```sql
-- find id in auth.users by email, then:
UPDATE auth.users SET role = 'operator' WHERE id = '<uuid>';
-- user must re-login to get new JWT
```

This uses Postgres `role` (not `app_metadata`). Ensure your Supabase JWT exposes `role` (default Supabase Auth does via `auth.users.role`).

## 4. Realtime

- Dashboard → Database → Replication → enable publication for `operations.orders` (the `REPLICA IDENTITY FULL` above is already in `OperationsPermissions.sql`).
- Client subscribes via `selectAsFlow()` in `SupabaseOperatorRepository` (`shared/operator/.../data/repository/SupabaseOperatorRepository.kt:32-91`): `observeAvailableOrders` / `observeActiveDeliveries` / `observeOrderHistory`. Requires logged-in operator or flows emit `[]`.

## 5. Orders: how they get created

`operations.orders` expects (see `OrdersTable.sql:11-24`):

`id, stripe_session_id UNIQUE, customer_email, customer_name, total_amount, currency, items jsonb, status delivery_status DEFAULT 'pending', shipping_address jsonb, operator_id FK auth.users, created_at, inventory_synced DEFAULT false`

The `stripe-checkout` Edge Function does **not** insert the order — it only creates the Stripe session with `metadata = {price_id: quantity}`. You must populate `operations.orders` from Stripe:

- Option A (recommended): Stripe webhook → your own Edge Function/server → `INSERT INTO operations.orders (stripe_session_id, ..., items = metadata, ...)` with service_role (bypasses RLS).
- Option B: read from Sync Engine’s synced checkout-session tables if you enabled them, then mirror into `operations.orders`.

`items` shape the operator app parses (`getAggregateManifest():128-168`): `{"price_abc": 2, "price_xyz": 1}` (string qty in Stripe metadata, int in `items` jsonb).

## 6. Deploy `stripe-checkout` Edge Function

Source: `supabase/edge-functions/stripe-checkout.ts` (note: folder is `edge-functions/`, not the CLI-default `functions/` — either rename to `functions/stripe-checkout/index.ts` or deploy with `--workdir`/dashboard).

```bash
supabase secrets set STRIPE_API_KEY=sk_... FRONTEND_URL=https://your-domain.com
supabase functions deploy stripe-checkout
```

Behavior (`stripe-checkout.ts:28-47`): `POST {items:[{price, quantity}]}` → `stripe.checkout.sessions.create({payment_method_types:['card'], line_items: items, metadata: {price: quantity}, mode:'payment', shipping_address_collection:{allowed_countries:['CA']}, success_url: FRONTEND_URL/success, cancel_url: FRONTEND_URL/cancel})` → `{url}`.

Before going public:

- Edit `allowed_countries: ['CA']` to your ship countries.
- Set `FRONTEND_URL` secret (falls back to `http://localhost:8080`).
- CORS is `*` — tighten `Access-Control-Allow-Origin` for prod.
- No auth/validation — client passes Price IDs directly; validate prices server-side if needed.

Client call: `CartViewModel.checkout()` (`shared/core/.../presentation/cart/CartViewModel.kt:137-149`) invokes `function = "stripe-checkout"`.

## 7. Client config — `local.properties` + BuildKonfig

Copy the template and fill it in (all keys are commented out — uncomment the lines you need):

```bash
cp local.properties.example local.properties
```

```properties
sdk.dir=C\:\\Users\\...\\Android\\Sdk
SUPABASE_URL=https://xyzcompany.supabase.co      # REQUIRED
SUPABASE_KEY=sb_publishable_...                  # REQUIRED
STORE_NAME=My Store                              # RECOMMENDED
MAX_CART_SIZE=25                                 # keep ≤ 30: cart contents are injected into Stripe metadata, which has a size limit
#VERIFICATION_MESSAGE=shown as blocking dialog on launch if non-empty (see App.kt)  # OPTIONAL
```

BuildKonfig is applied **only** in `shared/core/build.gradle.kts:8-15,117-150`, reading `local.properties` at project root.

Consumed in `data/remote/ClientFactory.kt:12-15` (`SUPABASE_URL/KEY` → `createSupabaseClient { install(Auth, Postgrest, Functions, Realtime) }`) and `Constants.kt:7-11` (`STORE_NAME`, `MAX_CART_SIZE`, `VERIFICATION_MESSAGE`). Missing keys default to `""` / `25` — rebuild after editing.

## 8. Run the apps

```bash
# Customer (Navigation3 storefront)
./gradlew :app-customer:wasmJsBrowserDevelopmentRun  # web wasm (fast)
./gradlew :app-customer:jsBrowserDevelopmentRun      # web js (compat)
./gradlew :app-customer:run                         # desktop (mainClass io.github.kmpstore.MainKt)
./gradlew :app-customer:assembleDebug               # android

# Operator (dashboard, no Navigation3)
./gradlew :app-operator:run
./gradlew :app-operator:assembleDebug

# iOS: open iosApp/ in Xcode (frameworks: iosArm64 + iosSimulatorArm64, isStatic=true)
```

First launch: `App.kt` creates SQLDelight driver per platform (Android `AndroidSqliteDriver`, JVM `JdbcSqliteDriver`, iOS `NativeSqliteDriver`, Web `WebWorkerDriver`), loads Koin modules, then `refreshProducts()` via `get_store_sync_payload()` into the local cache.

## 9. `collectBuilds` (root `build.gradle.kts:15-91`)

```bash
./gradlew collectBuilds   # output: !buildOutputs/<subproject>/{android,js,wasm,desktop}/
```

Runs `assemble` + `createDistributable` on all subprojects, wipes/creates `!buildOutputs/`, copies `build/outputs/apk/**/*.apk`, `build/dist/js|wasmJs/productionExecutable/`, `build/compose/binaries/main/app/`. Covers all systems **except iOS** — build iOS via Xcode (`iosArm64`/`iosSimulatorArm64`). Desktop output is whatever OS the build runs on (run it per-OS for Dmg/Msi/Deb). Skips subprojects with no outputs (warning only). `:server` jar is **not** collected — fetch from its `build/` dir.

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| Empty store, `products` view empty | Stripe product/price not `active`, or Sync Engine not syncing |
| Categories never appear | `metadata.categories` missing on Stripe product; check trigger `on_stripe_product_sync` exists |
| Operator sees no orders | Not promoted (`auth.users.role`), stale JWT (re-login), RLS not enabled, Realtime publication off |
| `UPDATE operations.orders` denied | Column-level grant only allows `operator_id, status` — don’t write other columns as operator |
| Checkout 400 / no URL | `STRIPE_API_KEY` secret missing, bad Price ID, `FRONTEND_URL` unset |
| `get_category_sync_payload` error | Make sure `StoreSyncPayload.sql` (both RPCs) was run; use full refresh as fallback |
| Blank after `local.properties` edit | Rebuild — BuildKonfig is compile-time |
