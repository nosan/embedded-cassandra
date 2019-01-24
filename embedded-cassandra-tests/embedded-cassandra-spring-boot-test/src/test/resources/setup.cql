CREATE KEYSPACE IF NOT EXISTS test  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE TABLE IF NOT EXISTS test.city  (
  id bigint PRIMARY KEY,
  name text
);

