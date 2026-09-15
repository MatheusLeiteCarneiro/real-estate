FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY api/pom.xml api/pom.xml

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY api/src api/src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system appgroup \
    && useradd --system --gid appgroup --no-create-home appuser

COPY --from=build --chown=appuser:appgroup \
    /app/api/target/real-estate-api.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
