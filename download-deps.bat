@echo off
REM ============================================
REM SocietEase — Dependency Download Script
REM Downloads all required JAR files to WEB-INF/lib
REM Run this from the project root directory.
REM ============================================

set LIB_DIR=web\WEB-INF\lib
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"

echo.
echo =============================================
echo  SocietEase — Downloading Dependencies
echo =============================================
echo.

REM JSTL 1.2 (for c:out XSS protection in JSPs)
echo [1/4] Downloading JSTL 1.2...
curl -L -o "%LIB_DIR%\jstl-1.2.jar" "https://repo1.maven.org/maven2/javax/servlet/jstl/1.2/jstl-1.2.jar"

REM Gson (JSON handling for API responses)
echo [2/4] Downloading Gson 2.10.1...
curl -L -o "%LIB_DIR%\gson-2.10.1.jar" "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"

REM JavaMail (already provided by GlassFish, but needed for standalone Tomcat)
echo [3/4] Downloading JavaMail 1.6.2...
curl -L -o "%LIB_DIR%\javax.mail-1.6.2.jar" "https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar"

REM MySQL Connector (backup copy in WEB-INF/lib)
echo [4/4] Downloading MySQL Connector J 9.2.0...
curl -L -o "%LIB_DIR%\mysql-connector-j-9.2.0.jar" "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.2.0/mysql-connector-j-9.2.0.jar"

echo.
echo =============================================
echo  All dependencies downloaded to %LIB_DIR%
echo =============================================
echo.
echo  Files downloaded:
dir /b "%LIB_DIR%"
echo.
echo  NOTE: If you are using GlassFish, javax.mail and jstl 
echo  may already be provided by the server. The copies in
echo  WEB-INF/lib ensure compatibility with Tomcat deployment.
echo.
echo  For Razorpay SDK (when ready for live payments):
echo  Download from: https://github.com/razorpay/razorpay-java/releases
echo  Place razorpay-java-1.4.6.jar in %LIB_DIR%
echo.
pause
