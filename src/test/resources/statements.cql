/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE KEYSPACE test
            WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
USE test;
CREATE TABLE users
(
    user_id    UUID PRIMARY KEY,
    name       TEXT,
    email      TEXT,
    country    TEXT,
    created_at TIMESTAMP
);

INSERT INTO users (user_id, name, email, country, created_at)
VALUES (uuid(), $$Alice$$, $$alice@example.com$$, $$Poland$$, toTimestamp(now()));

INSERT INTO users (user_id, name, email, country, created_at)
VALUES (uuid(), $$Carlos$$, $$carlos@example.com$$, $$Spain$$, toTimestamp(now()));
INSERT INTO users (user_id, name, email, country, created_at)
VALUES (uuid(), 'Bob', 'bob@example.com', 'Germany', toTimestamp(now()));
CREATE TABLE products
(
    product_id UUID PRIMARY KEY,
    name       TEXT,
    category   TEXT,
    price      DECIMAL,
    available  BOOLEAN
);
INSERT INTO products (product_id, name, category, price, available)
VALUES (uuid(), 'Wireless Mouse', 'Electronics', 19.99, true);

INSERT INTO products (product_id, name, category, price, available)
VALUES (uuid(), 'Coffee Mug', 'Kitchen', 7.50, true);
CREATE TABLE orders
(
    user_id    UUID,
    order_id   UUID,
    product_id UUID,
    quantity   INT,
    order_date TIMESTAMP,
    PRIMARY KEY (user_id, order_id)
) WITH CLUSTERING ORDER BY (order_id DESC);
INSERT INTO orders (user_id, order_id, product_id, quantity, order_date)
VALUES (uuid(), uuid(), uuid(), 2, toTimestamp(now()));

INSERT INTO orders (user_id, order_id, product_id, quantity, order_date)
VALUES (uuid(), uuid(), uuid(), 1, toTimestamp(now()));

