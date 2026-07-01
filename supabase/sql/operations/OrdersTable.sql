create schema if not exists operations;

create type operations.delivery_status as enum (
  'pending',
  'processing',
  'shipped',
  'delivered',
  'cancelled'
);

create table operations.orders (
    id uuid primary key default gen_random_uuid(),
    stripe_session_id text unique,
    customer_email text,
    customer_name text,
    total_amount bigint,
    currency text,
    items jsonb default '[]'::jsonb,
    status operations.delivery_status default 'pending',
    shipping_address jsonb,
    operator_id UUID references auth.users (id),
    created_at timestamptz default now(),
    inventory_synced boolean default false
);

-- Indexing for performance in the operator dashboard
create index idx_orders_status on operations.orders(status);
create index idx_orders_operator on operations.orders(operator_id);