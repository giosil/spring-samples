FROM eclipse-temurin:21

RUN mkdir -p /opt/app

COPY ./target/app-0.0.1-SNAPSHOT.jar /opt/app

EXPOSE 8080

ENTRYPOINT ["/opt/java/openjdk/bin/java", "-jar", "/opt/app/app-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]
