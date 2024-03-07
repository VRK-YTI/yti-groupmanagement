FROM yti-docker-java-base:corretto-11.0.22

RUN apk add --update git

ADD web-api/build/libs/yti-groupmanagement.jar yti-groupmanagement.jar

ENTRYPOINT ["/bootstrap.sh", "yti-groupmanagement.jar", "-j", "-Djava.security.egd=file:/dev/./urandom"]
