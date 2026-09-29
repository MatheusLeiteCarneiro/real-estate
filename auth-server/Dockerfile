FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY migrations/pom.xml migrations/pom.xml
COPY auth-server/pom.xml auth-server/pom.xml

RUN chmod +x mvnw
RUN ./mvnw install -N

COPY migrations/src migrations/src
RUN ./mvnw -f migrations/pom.xml install -DskipTests

RUN ./mvnw -f auth-server/pom.xml dependency:go-offline

COPY auth-server/src auth-server/src

RUN ./mvnw -f auth-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system appgroup \
    && useradd --system --gid appgroup --no-create-home appuser

COPY --from=build --chown=appuser:appgroup \
    /app/auth-server/target/real-estate-auth-server.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
