FROM openjdk:8-jre-slim
COPY ./app.jar /sandbox/advisor/app.jar
WORKDIR /sandbox/advisor
ENTRYPOINT exec java $APP_OPTS $SKYWALKING_OPTS -jar app.jar
