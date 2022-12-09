# Alpine Linux
FROM maven:3-eclipse-temurin-11-alpine as buildstage

# alternatively use RHEL UBI base image
# via https://catalog.redhat.com/software/containers/ubi9/openjdk-11/61ee7bafed74b2ffb22b07ab
# FROM registry.access.redhat.com/ubi9/openjdk-11 as buildstage

WORKDIR /app
COPY . /app
RUN mvn clean package

#######################################################################################################

# Alpine Linux
FROM eclipse-temurin:11-jre-alpine as runstage

# alternatively use RHEL UBI base image
# via https://catalog.redhat.com/software/containers/ubi9/openjdk-11-runtime/61ee7d1c33f211c45407a91c
# FROM registry.access.redhat.com/ubi9/openjdk-11-runtime as runstage

WORKDIR /app
ARG DEPENDENCY=/app/app/target/unpacked
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=buildstage ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp",".:lib/*","org.n52.sta.Application"]
