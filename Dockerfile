# FROM alpine/git
FROM maven:3.6.1-jdk-8-slim as buildstage
WORKDIR /app
COPY . /app/sensorweb-server-sta/

RUN cd sensorweb-server-sta \
    && mvn package

FROM adoptopenjdk/openjdk8:alpine-slim as runstage

ARG DEPENDENCY=/app/sensorweb-server-sta/app/target/unpacked
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=buildstage ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","org.n52.sta.Application"]
