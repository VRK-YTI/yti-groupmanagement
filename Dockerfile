FROM openjdk:8-jdk-alpine

ADD web-api/build/libs/rhp.jar rhp.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /rhp.jar" ]