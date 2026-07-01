-- switch someone to operator. find them in the auth.users table through email column or whatever they signed up with,
-- get their id and replace (copy-paste) bdeb6412-...-91d72eb0b9bd with their id in the following command:

-- UPDATE auth.users
-- SET role = 'operator'
-- WHERE id = 'bdeb6412-05cf-4501-be24-91d72eb0b9bd';