# LockerLine in a container. Two stages, so the machine that builds the image needs Docker and nothing else.

# 1. build the jar with the Maven Wrapper on a full JDK
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src/main src/main
RUN sh ./mvnw -q -B -DskipTests package

# 2. run it on a JRE: only the jar and the seed files go into this image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/app.jar app.jar
COPY seed seed
# a server inside a container must listen on every interface, or the host cannot reach it
ENV SERVER_ADDRESS=0.0.0.0
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
