# Imagem base do OpenJDK
FROM openjdk:17-jdk-slim

# Diretório de trabalho
WORKDIR /app

# Copiar o arquivo JAR gerado pela aplicação
COPY target/agendamento-api-0.0.1.jar app.jar

# Expor a porta da aplicação
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

