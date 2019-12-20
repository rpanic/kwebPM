FROM gradle:4.10.3-jdk8 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:8
COPY --from=builder /home/gradle/src/build/libs/kwebpm.jar-1.0.jar /app/kwebpm-1.0.jar
WORKDIR /app
EXPOSE 90
CMD ["java", "-jar", "kwebpm-1.0.jar"]

