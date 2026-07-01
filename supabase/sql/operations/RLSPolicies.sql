-- operations.orders

create policy "Enable operators to view their own data only"
on "operations"."orders"
as PERMISSIVE
for SELECT
to operator
using (
    ((( SELECT auth.uid() AS uid) = operator_id) OR (operator_id IS NULL))
);

create policy "Operators can update their own or unassigned orders"
on "operations"."orders"
as PERMISSIVE
for UPDATE
to operator
using (
    ((( SELECT auth.uid() AS uid) = operator_id) OR (operator_id IS NULL))
) with check (
    ((( SELECT auth.uid() AS uid) = operator_id) OR (operator_id IS NULL))
);