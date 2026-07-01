-- public.categories
create policy "Enable read access for all users"
on "public"."categories"
as PERMISSIVE
for SELECT
to public
using (
    true
);

-- public.category_products
create policy "Enable read access for all users"
on "public"."category_products"
as PERMISSIVE
for SELECT
to public
using (
    true
);