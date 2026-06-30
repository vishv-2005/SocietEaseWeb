FROM tomcat:9.0-jdk8-openjdk

# Remove default Tomcat apps to secure the installation and save space
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file to Tomcat webapps directory as ROOT.war
# This makes it accessible at the root URL (/) instead of (/SocietEaseWeb)
COPY dist/SocietEaseWeb.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
