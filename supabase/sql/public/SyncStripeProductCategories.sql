-- 1. Create the function that handles the logic
CREATE OR REPLACE FUNCTION public.sync_stripe_product_categories()
RETURNS TRIGGER AS $$
DECLARE
    cat_slug_raw text;
    cat_slug_trimmed text;
BEGIN
    -- 1. Wipe old associations to ensure the metadata is the "Source of Truth"
    DELETE FROM public.category_products WHERE stripe_product_id = NEW.id;

    -- 2. Check if metadata exists
    IF NEW.metadata->>'categories' IS NOT NULL THEN
        -- 3. Loop through the comma-separated string
        FOR cat_slug_raw IN SELECT unnest(string_to_array(NEW.metadata->>'categories', ',')) LOOP
            cat_slug_trimmed := trim(cat_slug_raw);

            -- 4. Auto-create the category if it doesn't exist
            INSERT INTO public.categories (name, slug)
            VALUES (
              initcap(cat_slug_trimmed), -- Capitalizes the slug for a "Pretty Name"
              cat_slug_trimmed
            )
            ON CONFLICT (slug) DO NOTHING;

            -- 5. Link the product to the category
            INSERT INTO public.category_products (category_id, stripe_product_id)
            SELECT id, NEW.id
            FROM public.categories
            WHERE slug = cat_slug_trimmed;
        END LOOP;
    END IF;

    -- 6. Add default inventory
    INSERT INTO operations.inventory (stripe_product_id, product_name, quantity, last_restocked, updated_at)
    SELECT NEW.id, NEW.name, 0, now(), now()
    ON CONFLICT (stripe_product_id) DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Attach the trigger to the stripe.products table
CREATE OR REPLACE TRIGGER on_stripe_product_sync
AFTER INSERT OR UPDATE ON stripe.products
FOR EACH ROW EXECUTE FUNCTION public.sync_stripe_product_categories();

-- 3. fake update triggers function for every existing product
UPDATE stripe.products SET name = default;