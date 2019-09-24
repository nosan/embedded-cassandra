CREATE KEYSPACE test WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1};
CREATE TABLE test.roles (   id text PRIMARY KEY );
