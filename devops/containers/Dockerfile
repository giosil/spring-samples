FROM maven:3-eclipse-temurin-19 as builder
WORKDIR /build
COPY src/ /build/
RUN mvn --settings /build/maven_settings.xml verify -f /build/pom.xml \
    && IMAGE_VERSION=`mvn --settings /build/maven_settings.xml -q -Dexec.executable=echo -Dexec.args='${project.artifactId}-${project.version}.jar' --non-recursive exec:exec 2>/dev/null` \
    && cp -v /build/target/${IMAGE_VERSION} /build/target/application.jar \
    && echo $IMAGE_VERSION > /build/target/application.version

FROM eclipse-temurin:19
USER root
RUN addgroup --gid 1000 java && adduser --system --home /app --gid 1000 --uid 1000  java
WORKDIR /app
COPY --from=builder --chown=java:java /build/target/application.* /app/
USER java
ENTRYPOINT /opt/java/openjdk/bin/java -Dhttps.protocols=TLSv1.2 \
    $JAVA_OPTS -jar /app/application.jar --spring.config.location=file:///app/config/application.properties
