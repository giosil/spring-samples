# Spring-samples

Spring-boot samples.

## Build frontend

Before install typescrpt, uglify-js and uglifycss

npm install -g typescript
npm install -g uglify-js
npm install -g uglifycss

To build:

npm run build

## Build application

mvn clean package

or

mvn clean package -DskipTests

## Run application

mvn spring-boot:run

mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8787"

or

java -jar ./target/app-0.0.1-SNAPSHOT.jar

## Run application with prod profile

mvn spring-boot:run -Dspring-boot.run.profiles=prod

or 

java -jar ./target/app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

## Build docker appbe image (Backend)

docker build -t appbe .

## Build docker appdb image (Database)

cd database

docker build -t appdb .

## Run erpbdb separately

docker run --name appdb-postgres -p 5432:5432 -d appdb

## Run postgres separately (empty database)

docker run --name appdb-postgres -p 5432:5432 -e POSTGRES_DB=appdb -e POSTGRES_USER=appdb -e POSTGRES_PASSWORD=passw0rd -d postgres:12

## Docker compose

docker compose -p "app-cluster" up --detach

See [more samples](README_more.md).

## Contributors

* [Giorgio Silvestris](https://github.com/giosil)