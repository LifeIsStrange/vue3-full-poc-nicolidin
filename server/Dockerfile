FROM gradle:jdk12 as builder
COPY --chown=gradle:gradle ./ /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootjar --no-daemon --configure-on-demand

FROM openjdk:12-jdk-alpine
EXPOSE 8080
EXPOSE 8085
COPY --from=builder /home/gradle/src/build/libs/rebirth-0.0.1-SNAPSHOT.jar /app/
WORKDIR /app/
CMD ["java", "-jar", "rebirth-0.0.1-SNAPSHOT.jar"]
