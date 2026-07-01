-- allow authenticator to recognize operator so that they can be authenticated
GRANT operator TO authenticator;

-- manage operator permissions
-- (they can select everything, but they can only update operator_id and status)
GRANT SELECT, UPDATE (operator_id, status) ON TABLE operations.orders TO operator;
GRANT USAGE ON SCHEMA operations TO operator;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA operations TO operator;
NOTIFY pgrst, 'reload config';

-- for realtime
ALTER TABLE operations.orders REPLICA IDENTITY FULL;