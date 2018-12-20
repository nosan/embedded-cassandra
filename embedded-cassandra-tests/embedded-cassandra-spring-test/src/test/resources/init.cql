CREATE KEYSPACE IF NOT EXISTS test  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE TABLE IF NOT EXISTS test.roles (   id text PRIMARY KEY );
CREATE TABLE IF NOT EXISTS test.users  (
  user_id text PRIMARY KEY,
  first_name text,
  last_name text,
  emails set<text>
);

