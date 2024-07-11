FROM postgres:12

ENV POSTGRES_DB=appdb
ENV POSTGRES_USER=appdb
ENV POSTGRES_PASSWORD=passw0rd

COPY psql_dump.sql /docker-entrypoint-initdb.d/