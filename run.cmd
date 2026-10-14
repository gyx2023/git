@echo off
REM Helper script: run Maven commands using the Maven bundled with IntelliJ IDEA.
REM Usage:  run.cmd spring-boot:run     -> start the server
REM         run.cmd test               -> run the tests
REM         run.cmd clean package      -> build a runnable jar

setlocal

if "%JAVA_HOME%"=="" set "JAVA_HOME=D:\jdk"

set "MVN=D:\idea\IntelliJ IDEA Community Edition 2024.2.3\plugins\maven\lib\maven3\bin\mvn.cmd"

if not exist "%MVN%" (
    echo [ERROR] Maven not found at:
    echo         %MVN%
    echo         Please edit the MVN line in this file so it points to your mvn.cmd
    exit /b 1
)

call "%MVN%" %*
