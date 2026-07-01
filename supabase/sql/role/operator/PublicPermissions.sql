-- 1. Allow the operator to see the public schema
GRANT USAGE ON SCHEMA public TO operator;

-- 2. Allow the operator to execute the specific RPC function
GRANT EXECUTE ON FUNCTION public.get_store_sync_payload() TO operator;

-- 3. Allow the operator to read from the products view
GRANT SELECT ON ALL TABLES IN SCHEMA public TO operator;

-- 4. Grant SELECT on all FUTURE tables and views automatically
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT ON TABLES TO operator;