-- clients will use this to collect all info they need to load up the store
create or replace function get_store_sync_payload()
returns json as $$
declare
    result json;
begin
    select json_build_object(
       'categories', (select coalesce(json_agg(c), '[]'::json) from categories c),
       'products', (select coalesce(json_agg(p), '[]'::json) from products p),
       'junctions', (select coalesce(json_agg(jp), '[]'::json) from category_products jp)
    ) into result;

    return result;
end;
$$ language plpgsql stable;