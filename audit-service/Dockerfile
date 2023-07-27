FROM eclipse-temurin:19-jre-focal

ADD ./target/audit_service.jar /app/

CMD ["java", "-Xmx200m", "-jar", "/app/audit_service.jar"]

EXPOSE 8080 8089