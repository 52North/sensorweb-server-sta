# FROM alpine/git

FROM alpine/git as gitstage
WORKDIR /app

RUN git clone https://github.com/speckij/olingo-odata4 \
    && cd olingo-odata4 \
    && git checkout v4.6.0-STA-alpha.1/fix/AnyTypeParsing

RUN git clone https://github.com/52North/sensor-things \
    && cd sensor-things \
    && git checkout master

RUN git clone https://github.com/52North/series-hibernate \
    && cd series-hibernate \
    && git checkout -q 572cede2a6e0e95aac5fc9e5602c8b26e1561720

FROM maven:3.6.1-jdk-8-slim as buildstage
WORKDIR /app
COPY --from=gitstage /app /app

RUN cd olingo-odata4 \
    && mvn -DskipTests=true install

RUN cd series-hibernate \
    && mvn install

RUN cd sensor-things \
    && sed -i 's/4.6.0-STA-alpha.2/4.6.0-STA-SNAPSHOT/g' pom.xml \
    && mvn package

FROM openjdk:8-jre-alpine as runstage
COPY --from=buildstage /app/sensor-things/sensor-things-api-app/target /target

ARG DEPENDENCY=target/unpacked
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","org.n52.sta.Application"]
