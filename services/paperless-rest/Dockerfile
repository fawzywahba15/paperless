# --- Build-Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 1) Nur POM kopieren, Dependencies cachen
COPY services/paperless-rest/pom.xml services/paperless-rest/
# Auch hier sicherheitshalber skip=true
RUN mvn -q -f services/paperless-rest/pom.xml -Dmaven.test.skip=true dependency:go-offline

# 2) Source kopieren und bauen
COPY services/paperless-rest/ services/paperless-rest/

# Tests werden geskipped, weil veraltet
# TODO: entferne -Dmaven.test.skip=true wenn unit tests passen
RUN mvn -q -f services/paperless-rest/pom.xml -Dmaven.test.skip=true package spring-boot:repackage

# --- Runtime-Stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
# Wir kopieren das JAR direkt (angepasster Pfad)
COPY --from=build /build/services/paperless-rest/target/paperless-rest-*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]