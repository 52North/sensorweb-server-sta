#FROM alpine/git as gitstage
#WORKDIR /app

#RUN git clone https://github.com/52north/arctic-sea \
#    && cd arctic-sea \
#    && git checkout master

#RUN git clone https://github.com/52north/sensorweb-server-db-model \
#    && cd sensorweb-server-db-model \
#    && git checkout develop

FROM maven:3-eclipse-temurin-11-alpine as buildstage
WORKDIR /app
#COPY --from=gitstage /app /app
COPY . /app/sensorweb-server-sta/

#RUN cd arctic-sea \
#    && mvn clean install

#RUN cd sensorweb-server-db-model \
#    && mvn clean install

RUN cd sensorweb-server-sta \
    && mvn package

FROM adoptopenjdk/openjdk11:alpine-slim as runstage

ARG DEPENDENCY=/app/sensorweb-server-sta/app/target/unpacked
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=buildstage ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=buildstage ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","org.n52.sta.Application"]
