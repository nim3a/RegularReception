@echo off
REM Set JAVA_HOME to your Java installation
set JAVA_HOME=C:\Program Files\Java\jdk-23

echo ========================================
echo   Starting Daryaft Core Application
echo ========================================
echo JAVA_HOME: %JAVA_HOME%
echo Port: 8081
echo.
echo Application will be available at:
echo   - Health Check: http://localhost:8081/api/health
echo   - H2 Console:   http://localhost:8081/h2-console
echo.
echo Press Ctrl+C to stop the application
echo ========================================
echo.

mvnw.cmd spring-boot:run
