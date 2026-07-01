-- only grab the necessary info from the stripe sync engine
CREATE OR REPLACE VIEW public.products AS
SELECT
    p.id,
    p.name,
    p.description,
    p.images[0] AS main_image,
    pr.unit_amount AS price,
    pr.currency,
    pr.id AS price_id
-- if you want to show customers how many items you have in stock
-- i.quantity AS stock
FROM stripe.products p
         JOIN stripe.prices pr ON p.id = pr.product
-- if you want to show customers how many items you have in stock
-- JOIN operations.inventory i ON p.id = i.stripe_product_id
WHERE p.active = true AND pr.active = true;

-- DROP VIEW IF EXISTS public.products;