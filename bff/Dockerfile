FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY bff/pom.xml bff/pom.xml

RUN chmod +x mvnw
RUN ./mvnw -f bff/pom.xml dependency:go-offline

COPY bff/src bff/src

RUN ./mvnw -f bff/pom.xml clean package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system appgroup \
    && useradd --system --gid appgroup --no-create-home appuser

COPY --from=build --chown=appuser:appgroup \
    /app/bff/target/real-estate-bff.jar app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
