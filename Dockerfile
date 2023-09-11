FROM alpine/git as gitstage

WORKDIR /app 

RUN git clone https://github.com/52north/sensorweb-server-db-model \
      && cd sensorweb-server-db-model \
      && git checkout v4.0.3


FROM maven:3-eclipse-temurin-11-alpine as buildstage
WORKDIR /app

COPY --from=gitstage /app /app
COPY . /app/sensorweb-server-sta/

RUN cd sensorweb-server-db-model \
      && mvn clean install

RUN cd sensorweb-server-sta \
      && mvn clean package

#######################################################################################################

# Alpine Linux
FROM eclipse-temurin:11-jre-alpine as runstage

LABEL maintainer="Jan Speckamp <j.speckamp@52north.org>" \
      org.opencontainers.image.title="52°North SensorThingsAPI" \
      org.opencontainers.image.description="52°North SensorThingsAPI" \
      org.opencontainers.image.licenses="GPLv2" \
      org.opencontainers.image.url="https://github.com/52North/sensorweb-server-sta" \
      org.opencontainers.image.vendor="52°North GmbH" \
      org.opencontainers.image.source="https://github.com/52North/sensorweb-server-sta.git" \
      org.opencontainers.image.authors="Jan Speckamp <j.speckamp@52north.org>"

WORKDIR /app
ARG DEPENDENCY=/app/sensorweb-server-sta/app/target/unpacked
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=buildstage ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp",".:lib/*","org.n52.sta.Application"]