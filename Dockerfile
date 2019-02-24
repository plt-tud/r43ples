FROM openjdk:8-jre-alpine

MAINTAINER Markus Graube <markus.graube@tu-dresden.de>

ADD target/r43ples.jar .
ADD src/main/resources/r43ples.dist.conf r43ples.conf

EXPOSE 9998

CMD ["java", "-jar", "r43ples.jar"]
