## Run postgres image (empty DB)

docker run --name appdb-postgres -e POSTGRES_DB=appdb -e POSTGRES_USER=appdb -e POSTGRES_PASSWORD=passw0rd -p 5432:5432 -d postgres:12

docker exec -it appdb-postgres /bin/bash

## Build docker appdb image (Database)

docker build -t appdb .

## Run appdb image

docker run --name appdb-postgres -p 5432:5432 -d appdb

## Check postgresql

psql postgresql://localhost:5432/appdb -U appdb

```
	SELECT schema_name FROM information_schema.schemata;
	
	SELECT table_name FROM inexitformation_schema.tables WHERE table_schema = 'public';
	
	\h            (help)
	\l            (list databases)
	\dt           (display tables)
	\c [db]       (connect to a database)
	\d [table]    (display schema of [table])
	\du           (display user roles)
	\q            (quit)
```

## Dump 

pg_dump --host localhost --port 5432 --username appdb --dbname appdb > appdb_backup.sql

or

pg_dump -h localhost -p 5432 -U appdb -d appdb > appdb_backup.sql

## Restore

pg_restore --host localhost --port 5432 --username appdb --dbname appdb appdb_backup.sql

or

pg_restore -h localhost -p 5432 -U appdb -d appdb appdb_backup.sql
