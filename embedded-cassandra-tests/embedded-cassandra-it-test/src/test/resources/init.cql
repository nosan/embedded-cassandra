CREATE KEYSPACE test  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE TABLE test.roles (   id text PRIMARY KEY );
CREATE TABLE test.users  (
  user_id text PRIMARY KEY,
  first_name text,
  last_name text,
  emails set<text>
);

INSERT INTO "test"."users" ("user_id", "first_name", "last_name", "emails")
    VALUES ('frodo', 'Frodo', 'Baggins', {'f@baggins.com', 'baggins@gmail.com'});

INSERT INTO "test"."roles" ("id") VALUES ('test_id');

CREATE INDEX usersfistnameindex ON test.users (first_name);
CREATE INDEX userssecondnameindex ON test.users (last_name);
