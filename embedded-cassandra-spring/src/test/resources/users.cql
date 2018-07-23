CREATE TABLE IF NOT EXISTS test.users  (
  user_id text PRIMARY KEY,
  first_name text,
  last_name text,
  emails set<text>
);

