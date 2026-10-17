# ============================================================
#  Étape 1 : compilation (JDK + Maven Wrapper)
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# 1) D'abord le wrapper Maven et le pom.xml : couche mise en cache
#    tant que les dépendances ne changent pas
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# 2) Ensuite le code source
COPY src src
RUN ./mvnw -B -q -DskipTests package && cp target/taskflow-api-*.jar app.jar

# ============================================================
#  Étape 2 : exécution (JRE seul, utilisateur non root)
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S taskflow && adduser -S taskflow -G taskflow

COPY --from=build /workspace/app.jar app.jar

USER taskflow
EXPOSE 8081

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]