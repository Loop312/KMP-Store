create table operations.inventory (
    -- Link it directly to the Stripe Product ID
    stripe_product_id text primary key references stripe.products(id) on delete cascade,
    product_name text,
    quantity int not null default 0,
    last_restocked timestamptz default now(),
    updated_at timestamptz default now()
);

-- drop table if exists operations.inventory;

-- this is to automatically fill the inventory if stripe setup was handled first
-- insert into operations.inventory (stripe_product_id, product_name, quantity, last_restocked, updated_at)
-- select
--   id as stripe_product_id,
--   name as product_name,
--   0 as quantity,          -- Default starting stock to 0
--   now() as last_restocked,
--   now() as updated_at
-- from stripe.products
-- on conflict (stripe_product_id) do nothing;