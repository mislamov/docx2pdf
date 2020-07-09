FROM maven:3.5.2-jdk-8
WORKDIR /usr/src/app
COPY src /usr/src/app/src
COPY lib /usr/src/app/lib
COPY pom.xml /usr/src/app
RUN mvn package
EXPOSE 8080
