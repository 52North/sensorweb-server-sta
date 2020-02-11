# FROM alpine/git

FROM alpine/git as gitstage
WORKDIR /app

RUN git clone https://github.com/speckij/olingo-odata4 \
    && cd olingo-odata4 \
    && git checkout v4.6.0-STA

RUN git clone https://github.com/52North/sensor-things \
    && cd sensor-things \
    && git checkout v1.0.0

RUN git clone https://github.com/52North/series-hibernate \
    && cd series-hibernate \
    && git checkout v2.0.0-alpha.5

FROM maven:3.6.1-jdk-8-slim as buildstage
WORKDIR /app
COPY --from=gitstage /app /app

RUN cd olingo-odata4 \
    && mvn -DskipTests=true install

RUN cd series-hibernate \
    && mvn install

RUN cd sensor-things \
    && mvn package

FROM openjdk:8-jre-alpine as runstage

ARG DEPENDENCY=/app/sensor-things/sensor-things-api-app/target/unpacked
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=buildstage ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/classes /app
COPY --from=buildstage /app/sensor-things/sensor-things-api-core/src/main/resources/META-INF/persistence.xml /app/
ENTRYPOINT ["java","-cp","app:app/lib/*","org.n52.sta.Application"]
