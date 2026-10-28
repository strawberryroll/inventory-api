# ---------- 1단계: 빌드 ----------
# JDK(개발 도구 포함)가 들어 있는 이미지에서 실행 가능한 jar를 만든다.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 다운로드를 캐시하려고 빌드 설정 파일만 먼저 복사한다.
# (소스만 바뀌었을 때는 이 단계를 다시 실행하지 않아서 빌드가 빨라진다)
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies > /dev/null

COPY src src
# 테스트는 이미지를 만들 때가 아니라 따로(./gradlew test) 실행한다.
RUN ./gradlew --no-daemon bootJar -x test

# ---------- 2단계: 실행 ----------
# 실행에는 JRE(실행 전용, 더 작음)만 있으면 되므로 빌드 도구는 버리고 jar만 가져온다.
FROM eclipse-temurin:21-jre
WORKDIR /app

# root가 아닌 일반 사용자로 실행한다. (컨테이너가 뚫려도 피해를 줄이기 위해서)
RUN useradd --system --uid 1001 appuser
COPY --from=build /app/build/libs/*.jar app.jar
USER appuser

# 작은 서버(메모리 512MB, CPU 0.1~0.5)에 맞춘 JVM 옵션
#  - MaxRAMPercentage=60 : 힙 크기를 컨테이너 메모리의 60%로 제한해서 메모리 초과로 죽지 않게 한다
#  - UseSerialGC, Xss512k : 메모리를 적게 쓰는 GC와 작은 스레드 스택
#  - TieredStopAtLevel=1  : JIT 컴파일을 가벼운 단계까지만 사용해 시작 속도를 높인다.
#                           (CPU 0.1 조건에서 시작 시간이 약 194초 -> 97초로 줄었다. 대신 오래 실행했을 때의 최고 성능은 낮아짐)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
