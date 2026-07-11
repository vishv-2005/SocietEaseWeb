FROM tomcat:9.0-jdk8-openjdk

# Remove default Tomcat apps to secure the installation and save space
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the pre-built WAR file from your local NetBeans dist folder
COPY dist/SocietEaseWeb.war /usr/local/tomcat/webapps/ROOT.war

# Limit JVM memory so it doesn't overshoot the container's 1GB RAM limit
ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080
CMD ["catalina.sh", "run"]
