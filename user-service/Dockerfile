FROM eclipse-temurin:19-jre-focal


ADD ./target/user_service.jar /app/

CMD ["java", "-Xmx200m", "-jar", "/app/user_service.jar"]

EXPOSE 8080 8089