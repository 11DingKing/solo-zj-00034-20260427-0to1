FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 配置阿里云 Maven 镜像源
RUN mkdir -p /root/.m2 && \
    echo '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/central</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
