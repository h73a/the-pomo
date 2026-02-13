# -------- Node stage: Tailwind build --------
FROM node:18 AS node-builder
WORKDIR /app

# package.jsonとlockを先にコピー
COPY app/package*.json ./
RUN npm install

# Tailwind設定やCSSソースをコピー
COPY app/tailwind.config.js ./
COPY app/postcss.config.js ./
COPY app/src ./src

# CSSビルド
RUN npm run build:css


# -------- Maven stage: Spring Boot package --------
FROM maven:3.9.5-eclipse-temurin-17 AS maven-builder
WORKDIR /app

COPY app/pom.xml .
RUN mvn dependency:go-offline

COPY app .
# NodeでビルドしたCSSをstatic配下へコピー
COPY --from=node-builder /app/src/main/resources/static/css/output.css ./src/main/resources/static/css/output.css

RUN mvn clean package -DskipTests


# -------- Runtime stage --------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=maven-builder /app/target/learning_tracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
