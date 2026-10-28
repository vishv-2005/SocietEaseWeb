# ---- Build stage: compile from source (no committed WAR needed) ----
FROM tomcat:9.0-jdk8-openjdk AS build
WORKDIR /app
COPY src ./src
COPY web ./web

RUN set -eux; \
    mkdir -p war/WEB-INF/classes; \
    cp -r web/. war/; \
    find src/java -name '*.java' > sources.txt; \
    javac -encoding UTF-8 -nowarn \
      -cp "/usr/local/tomcat/lib/*:web/WEB-INF/lib/*" \
      -d war/WEB-INF/classes @sources.txt; \
    (cd src/java && find . -type f ! -name '*.java' -exec cp --parents {} /app/war/WEB-INF/classes/ \;); \
    cd war && jar cf /app/ROOT.war .

# ---- Runtime stage ----
FROM tomcat:9.0-jdk8-openjdk

# Remove default Tomcat apps to secure the installation and save space
RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/ROOT.war /usr/local/tomcat/webapps/ROOT.war

# Limit JVM memory so it doesn't overshoot the container's 1GB RAM limit
ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080
CMD ["catalina.sh", "run"]
