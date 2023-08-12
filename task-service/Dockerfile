FROM eclipse-temurin:19-jre-focal

ADD ./target/task_service.jar /app/

CMD ["java", "-Xmx200m", "-jar", "/app/task_service.jar"]

EXPOSE 8080 8089