# Utilisation de l'image officielle Java pour Spring Boot
FROM openjdk:21-jdk-slim

# Ajout de l'application JAR à l'image Docker
COPY target/*.jar /app/app.jar

# Exposition du port sur lequel l'application va écouter
EXPOSE 8080

# Commande pour exécuter l'application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=debug", "--spring.config.location=/app/config/application.properties"]
