FROM openjdk:11-jdk 

ADD target/firsttrial-0.0.1-SNAPSHOT.jar app.jar 
 
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar 