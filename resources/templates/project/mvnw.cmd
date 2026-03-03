@echo off
setlocal

set BASE_DIR=%~dp0
set WRAPPER_JAR=%BASE_DIR%.mvn\wrapper\maven-wrapper.jar

if not exist "%WRAPPER_JAR%" (
  echo Missing %WRAPPER_JAR%. Run: mvn -N wrapper:wrapper
  exit /b 1
)

java -jar "%WRAPPER_JAR%" %*