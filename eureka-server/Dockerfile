FROM adoptopenjdk/openjdk13
MAINTAINER roman
COPY target/eureka-server-0.0.1-SNAPSHOT.jar /opt/lib/
RUN mkdir /var/lib/facebook
CMD ["java", "-jar", "/opt/lib/eureka-server-0.0.1-SNAPSHOT.jar"]
VOLUME /var/lib/facebook
EXPOSE 8761

