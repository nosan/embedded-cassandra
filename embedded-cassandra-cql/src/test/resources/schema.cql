CREATE KEYSPACE test  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE TABLE test.roles (   id text PRIMARY KEY );
CREATE TABLE test.users  (
  user_id text PRIMARY KEY,
  first_name text,
  last_name text,
  emails set<text>
);

