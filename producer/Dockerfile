FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src
CMD ["./mvnw", "compile", "exec:java", "-Dexec.mainClass=rosariob.kafka.VehiclePositionProducer"]