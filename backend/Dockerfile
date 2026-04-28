FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 配置阿里云 + 腾讯云双镜像源
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\
<settings>\
  <mirrors>\
    <mirror>\
      <id>aliyun</id>\
      <mirrorOf>central</mirrorOf>\
      <url>https://maven.aliyun.com/repository/central</url>\
    </mirror>\
  </mirrors>\
  <profiles>\
    <profile>\
      <id>default</id>\
      <repositories>\
        <repository>\
          <id>tencent</id>\
          <url>https://mirrors.cloud.tencent.com/nexus/repository/maven-public/</url>\
        </repository>\
      </repositories>\
    </profile>\
  </profiles>\
  <activeProfiles>\
    <activeProfile>default</activeProfile>\
  </activeProfiles>\
</settings>' > /root/.m2/settings.xml

# 先拷贝 pom，单独下载依赖（这层会被 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:resolve -B -Dmaven.wagon.http.retryHandler.count=10 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true || \
    mvn dependency:resolve -B -Dmaven.wagon.http.retryHandler.count=10 || \
    mvn dependency:resolve -B || true

RUN mvn dependency:resolve-plugins -B || true

# 拷贝源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B -Dmaven.wagon.http.retryHandler.count=10 || \
    mvn clean package -DskipTests -B -Dmaven.wagon.http.retryHandler.count=10 || \
    mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
