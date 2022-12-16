# Alpine Linux
#FROM maven:3-eclipse-temurin-11-alpine as buildstage

# alternatively use RHEL UBI base image
# via https://catalog.redhat.com/software/containers/ubi9/openjdk-11/61ee7bafed74b2ffb22b07ab
FROM registry.access.redhat.com/ubi8/openjdk-11 as buildstage
USER root
WORKDIR /app
COPY . /app
RUN mvn clean package

#######################################################################################################

# Alpine Linux
#FROM eclipse-temurin:11-jre-alpine as runstage

# alternatively use RHEL UBI base image
# via https://catalog.redhat.com/software/containers/ubi9/openjdk-11-runtime/61ee7d1c33f211c45407a91c
FROM registry.access.redhat.com/ubi8/openjdk-11-runtime as runstage
USER 1001
WORKDIR /app

LABEL maintainer="Jan speckamp <j.speckamp@52north.org>" \
      org.opencontainers.image.title="52°North SensorThingsAPI" \
      org.opencontainers.image.description="52°North SensorThingsAPI" \
      org.opencontainers.image.licenses="GPLv2" \
      org.opencontainers.image.url="https://github.com/52North/sensorweb-server-sta" \
      org.opencontainers.image.vendor="52°North GmbH" \
      org.opencontainers.image.source="https://github.com/52North/sensorweb-server-sta.git" \
      org.opencontainers.image.authors="Jan Speckamp <j.speckamp@52north.org>"

ARG DEPENDENCY=/app/app/target/unpacked
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=buildstage ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp",".:lib/*","org.n52.sta.Application"]
