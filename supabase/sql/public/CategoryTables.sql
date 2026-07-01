-- this is where all the categories/sub-categories will be stored.
create table public.categories (
    id uuid primary key default gen_random_uuid (),
    name text not null,
    slug text unique not null,
    parent_id uuid references public.categories(id) ON DELETE SET NULL,
    constraint chk_not_own_parent check (id <> parent_id)
);

-- if you want to add any recursive functions to the server
-- CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON public.categories(parent_id);

-- junction table for categories and products
create table public.category_products (
  category_id uuid references public.categories (id) on delete cascade,
  stripe_product_id text references stripe.products (id) on delete cascade,
  PRIMARY KEY (category_id, stripe_product_id)
);