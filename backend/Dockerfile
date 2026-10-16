FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn -B verify
FROM eclipse-temurin:21-jre
RUN groupadd --system gusim && useradd --system --uid 10001 --gid gusim gusim && mkdir -p /data/uploads && chown -R gusim:gusim /data
WORKDIR /app
COPY --from=build /build/target/gusim-api-1.0.0.jar app.jar
USER gusim
ENV UPLOAD_DIR=/data/uploads
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
