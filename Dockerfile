FROM gradle:7.4-jdk17 as builder
USER root
WORKDIR /builder
ADD . /builder
RUN ["gradle", "clean", "MyFatJar"]



FROM eclipse-temurin:17-jdk-alpine

ENV MY_VARIABLE_1=value1\
    MY_VARIABLE_2=value2

WORKDIR /JavaBlockchainForEXP


COPY --from=builder /builder/build/libs/JavaBlockchainForEXP-1.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "JavaBlockchainForEXP-1.0-SNAPSHOT.jar"]