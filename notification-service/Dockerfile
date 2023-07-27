FROM eclipse-temurin:19-jre-focal

ADD ./target/notification_service.jar /app/

CMD ["java", "-Xmx200m", "-jar", "/app/notification_service.jar"]

EXPOSE 8080 8089