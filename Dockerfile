#FROM openjdk:11-jdk
#FROM openjdk:11-jdk-slim
FROM adoptopenjdk/openjdk11:alpine-jre
#FROM adoptopenjdk:11-jre-hotspot

ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} /app.jar

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar 

## docker build --tag jo9999ki/firsttrial:v1 .
## docker image ls
## docker run -d -p 8080:8080 --rm --name v1 jo9999ki/firsttrial:v1
## docker run -d -p 80:8080 --rm --name v1 jo9999ki/firsttrial:v1
## docker run -d -p 443:8443 --rm --name v1 jo9999ki/firsttrial:v1
## docker ps
## docker kill v1